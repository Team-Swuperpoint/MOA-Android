package com.swuperpoint.moa_android.viewmodel.main.timeline

import android.content.Context
import android.location.Geocoder
import android.media.ExifInterface
import android.net.Uri
import android.provider.MediaStore
import android.telecom.Call
import android.util.Log
import android.view.WindowInsetsAnimation
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.swuperpoint.moa_android.data.remote.model.group.CreateTimelineGatheringResponse
import com.swuperpoint.moa_android.data.remote.model.group.CreateTimelineGroupResponse
import com.swuperpoint.moa_android.data.remote.model.group.CreateTimelineInfoResponse
import com.google.firebase.firestore.FieldValue
import com.google.firebase.ktx.Firebase
import com.kakao.vectormap.BuildConfig
import com.swuperpoint.moa_android.data.remote.model.group.kakaomap.Coord2AddressResponse
import com.swuperpoint.moa_android.data.remote.model.group.kakaomap.KakaoMapClass
import com.swuperpoint.moa_android.data.remote.model.group.kakaomap.SearchAddressResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/* 타임라인 만들기 화면의 뷰모델 */
class CreateTimelineViewModel: ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _response = MutableLiveData<CreateTimelineInfoResponse>()
    val response: LiveData<CreateTimelineInfoResponse> get() = _response

    private val _groupList = MutableLiveData<ArrayList<CreateTimelineGroupResponse>>()
    val groupList: LiveData<ArrayList<CreateTimelineGroupResponse>> = _groupList

    private val _gatheringList = MutableLiveData<ArrayList<CreateTimelineGatheringResponse>>()
    val gatheringList: LiveData<ArrayList<CreateTimelineGatheringResponse>> = _gatheringList

    // 그룹 목록 조회
    fun fetchCreateTimeline() {
        db.collection("groups").get()
            .addOnSuccessListener { groupSnapshot ->
                val groups = ArrayList(groupSnapshot.documents.map { doc ->
                    CreateTimelineGroupResponse(
                        groupId = doc.id,
                        groupName = doc.getString("groupName") ?: ""
                    )
                })
                _groupList.value = groups

                groups.firstOrNull()?.let {
                    fetchGatheringsForGroup(it.groupId)
                }
            }
    }

    // 특정 그룹의 모임 목록 조회
    fun fetchGatheringsForGroup(groupId: String) {
        db.collection("groups")
            .document(groupId)
            .collection("gatherings")
            .get()
            .addOnSuccessListener { gatheringSnapshot ->
                val gatherings = ArrayList(gatheringSnapshot.documents.map { doc ->
                    CreateTimelineGatheringResponse(
                        gatheringId = doc.id,
                        gatheringName = doc.getString("gatheringName") ?: ""
                    )
                })
                _gatheringList.value = gatherings
            }
            .addOnFailureListener { e ->
                Log.e("Timeline", "모임 정보 조회 중 오류 발생: ", e)
            }
    }

    // 타임라인 생성
    fun createTimeline(
        groupId: String,
        groupName: String,
        gatheringId: String,
        gatheringName: String,
        photoUris: List<Uri>,
        context: Context,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // 모임 정보 조회
                val gatheringDoc = withContext(Dispatchers.IO) {
                    db.collection("groups").document(groupId)
                        .collection("gatherings").document(gatheringId)
                        .get()
                        .await()
                }

                // 모임 장소 정보 추출
                val gatheringPlace = gatheringDoc.get("gatheringPlace") as? Map<String, Any>
                val placeName = gatheringPlace?.get("placeName") as? String
                val finalPlaceName = placeName ?: "장소 미정"

                // 사진 속 메타 데이터 추출 및 스토리지 업로드 병렬 처리
                val photoMetadataList = photoUris.map { uri ->
                    async(Dispatchers.IO) {
                        getPhotoMetadata(uri, context)
                    }
                }.awaitAll()

                val uploadedPhotos = photoUris.map { uri ->
                    async(Dispatchers.IO) {
                        uploadPhotoToStorage(uri)
                    }
                }.awaitAll().filterNotNull()

                if (uploadedPhotos.isNotEmpty()) {
                    // 시간순으로 사진 정렬
                    val dateFormat = SimpleDateFormat("a h시 mm분", Locale.KOREA)
                    val sortedPhotos = photoMetadataList.zip(uploadedPhotos)
                        .sortedBy { (metadata, _) ->
                            dateFormat.parse(metadata.time)
                        }

                    // 타임라인 데이터 구성
                    val timelineData = hashMapOf(
                        "date" to SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(Date()),
                        "placeName" to finalPlaceName,
                        "groupId" to groupId,
                        "groupName" to groupName,
                        "gatheringId" to gatheringId,
                        "gatheringName" to gatheringName,
                        "groupMemberNum" to 0,
                        "gatheringImgURL" to uploadedPhotos.first(),
                        "photos" to sortedPhotos.map { (metadata, photoUrl) ->
                            hashMapOf(
                                "time" to metadata.time,
                                "latitude" to metadata.latitude,
                                "longitude" to metadata.longitude,
                                "photoList" to listOf(photoUrl)
                            )
                        },
                        "createdAt" to FieldValue.serverTimestamp(),
                        "createdBy" to Firebase.auth.currentUser?.uid
                    )

                    // Firestore에 타임라인 데이터 저장
                    val documentRef = withContext(Dispatchers.IO) {
                        db.collection("groups").document(groupId)
                            .collection("gatherings").document(gatheringId)
                            .collection("timelines")
                            .add(timelineData)
                            .await()
                    }

                    onSuccess(documentRef.id)
                }
            } catch (e: Exception) {
                Log.e("Timeline", "타임라인 생성 중 오류 발생", e)
            }
        }
    }

    // 사진 속 메타 데이터(시간, 위치) 추출
    private suspend fun getPhotoMetadata(uri: Uri, context: Context): PhotoMetadata {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val exif = ExifInterface(inputStream!!)

                var latitude: Double? = null
                var longitude: Double? = null

                // EXIF에서 GPS 정보 추출
                val latitudeStr = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE)
                val latitudeRef = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF)
                val longitudeStr = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)
                val longitudeRef = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF)

                // GPS 좌표 변환
                if (latitudeStr != null && longitudeStr != null) {
                    try {
                        latitude = convertToDegree(latitudeStr)
                        if (latitudeRef == "S") {
                            latitude = -latitude
                        }

                        longitude = convertToDegree(longitudeStr)
                        if (longitudeRef == "W") {
                            longitude = -longitude
                        }
                    } catch (e: Exception) {
                        Log.e("Timeline", "GPS 좌표 변환 중 오류 발생", e)
                    }
                }

                // 시간 정보 추출
                var dateTime = SimpleDateFormat("a h시 mm분", Locale.KOREA).format(Date())
                exif.getAttribute(ExifInterface.TAG_DATETIME)?.let { exifDateTime ->
                    try {
                        val parser = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault())
                        val date = parser.parse(exifDateTime)
                        dateTime = SimpleDateFormat("a h시 mm분", Locale.KOREA).format(date!!)
                    } catch (e: Exception) {
                        Log.e("Timeline", "EXIF 시간 정보 파싱 중 오류 발생", e)
                    }
                }

                // MediaStore에서 위치 정보 조회 시도
                if (latitude == null || longitude == null) {
                    val projection = arrayOf(
                        MediaStore.Images.Media.DATE_TAKEN,
                        MediaStore.Images.Media.LATITUDE,
                        MediaStore.Images.Media.LONGITUDE
                    )

                    context.contentResolver.query(
                        uri,
                        projection,
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            try {
                                val latIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.LATITUDE)
                                val longIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.LONGITUDE)

                                latitude = cursor.getDouble(latIndex)
                                longitude = cursor.getDouble(longIndex)

                                // 유효하지 않은 좌표값 처리
                                if (latitude == 0.0 && longitude == 0.0) {
                                    latitude = null
                                    longitude = null
                                }
                            } catch (e: Exception) {
                                Log.e("Timeline", "MediaStore 데이터 읽기 중 오류 발생", e)
                            }
                        }
                    }
                }

                inputStream.close()

                // 카카오맵 API로 주소 정보 조회
                var locationName: String? = null
                if (latitude != null && longitude != null) {
                    try {
                        val response = suspendCancellableCoroutine<Coord2AddressResponse?> { continuation ->
                            KakaoMapClass.api.getAddressFromCoords(
                                key = KakaoMapClass.API_KEY,
                                longitude = longitude.toString(),
                                latitude = latitude.toString()
                            ).enqueue(object : retrofit2.Callback<Coord2AddressResponse> {
                                override fun onResponse(
                                    call: retrofit2.Call<Coord2AddressResponse>,
                                    response: retrofit2.Response<Coord2AddressResponse>
                                ) {
                                    continuation.resume(response.body())
                                }

                                override fun onFailure(call: retrofit2.Call<Coord2AddressResponse>, t: Throwable) {
                                    Log.e("Timeline", "카카오맵 API 호출 실패", t)
                                    continuation.resume(null)
                                }
                            })
                        }

                        locationName = response?.documents?.firstOrNull()?.let { document ->
                            when {
                                document.road_address?.building_name?.isNotEmpty() == true ->
                                    document.road_address.building_name
                                document.road_address?.address_name?.isNotEmpty() == true ->
                                    document.road_address.address_name.split(" ").last()
                                document.address?.address_name?.isNotEmpty() == true ->
                                    document.address.address_name.split(" ").last()
                                else -> null
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("Timeline", "카카오맵 주소 변환 중 오류 발생", e)
                    }
                }

                PhotoMetadata(
                    time = dateTime,
                    latitude = latitude,
                    longitude = longitude,
                    placeName = locationName ?: "위치 정보 없음"
                )
            } catch (e: Exception) {
                Log.e("Timeline", "사진 메타데이터 추출 중 오류 발생", e)
                PhotoMetadata(
                    time = SimpleDateFormat("a h시 mm분", Locale.KOREA).format(Date()),
                    latitude = null,
                    longitude = null,
                    placeName = "위치 정보 없음"
                )
            }
        }
    }

    // GPS 좌표 문자열을 도(degree) 단위로 변환
    private fun convertToDegree(stringDMS: String): Double {
        val parts = stringDMS.split(",")
        var result = 0.0

        // 도(degrees) 변환
        val degrees = parts[0].split("/")
        if (degrees.size == 2) {
            result += degrees[0].toDouble() / degrees[1].toDouble()
        }

        // 분(minutes) 변환
        if (parts.size >= 2) {
            val minutes = parts[1].trim().split("/")
            if (minutes.size == 2) {
                result += (minutes[0].toDouble() / minutes[1].toDouble()) / 60.0
            }
        }

        // 초(seconds) 변환
        if (parts.size >= 3) {
            val seconds = parts[2].trim().split("/")
            if (seconds.size == 2) {
                result += (seconds[0].toDouble() / seconds[1].toDouble()) / 3600.0
            }
        }

        return result
    }

    // 사진을 Firebase Storage에 업로드
    private suspend fun uploadPhotoToStorage(uri: Uri): String? = suspendCoroutine { continuation ->
        val filename = "timeline_photos/${UUID.randomUUID()}"
        val photoRef = storage.reference.child(filename)

        photoRef.putFile(uri)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                photoRef.downloadUrl
            }
            .addOnSuccessListener { downloadUri ->
                continuation.resume(downloadUri.toString())
            }
            .addOnFailureListener {
                continuation.resume(null)
            }
    }

    data class PhotoMetadata(
        val time: String,
        val latitude: Double?,
        val longitude: Double?,
        val placeName: String?
    )
}