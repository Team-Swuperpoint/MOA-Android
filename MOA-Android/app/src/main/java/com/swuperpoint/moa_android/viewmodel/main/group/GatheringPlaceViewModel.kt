package com.swuperpoint.moa_android.viewmodel.main.group

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.content.Context
import android.util.Log
import com.swuperpoint.moa_android.data.remote.model.group.GatheringPlaceResponse
import com.swuperpoint.moa_android.data.remote.model.group.MemberResponse
import com.swuperpoint.moa_android.data.remote.model.group.PlaceLocationResponse
import com.swuperpoint.moa_android.data.remote.model.group.RecommendPlaceResponse
import com.swuperpoint.moa_android.view.main.group.data.AddressItem
import com.swuperpoint.moa_android.view.main.group.data.MemberItem
import com.swuperpoint.moa_android.util.Coordinate
import com.swuperpoint.moa_android.util.MidpointCalculator
import kotlinx.coroutines.launch

class GatheringPlaceViewModel : ViewModel() {
    private val db = Firebase.firestore
    private lateinit var midpointCalculator: MidpointCalculator

    private val _response = MutableLiveData<GatheringPlaceResponse>()
    val response: LiveData<GatheringPlaceResponse> get() = _response

    // 로딩 상태
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // 에러 메시지
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    var memberList: LiveData<List<MemberItem>> = _response.map { response ->
        response.memberList.map { member ->
            MemberItem(
                memberId = member.memberId,
                profileImgURL = member.profileImgURL,
                memberName = member.memberName,
                isEdit = false
            )
        }
    }

    var memberNum: LiveData<Int> = _response.map { it.memberNum }

    private val _addressItem = MutableLiveData<AddressItem?>()
    val addressItem: LiveData<AddressItem?> get() = _addressItem

    var userPlaceName: LiveData<String?> = MediatorLiveData<String?>().apply {
        addSource(_response.map { it.userPlaceName }) { value = it }
        addSource(_addressItem) { new ->
            value = new?.title ?: _response.value?.userPlaceName
        }
    }

    var startPlace: LiveData<PlaceLocationResponse?> = _response.map { it.startPlace }

    var placeList: LiveData<ArrayList<RecommendPlaceResponse>?> = _response.map { it.placeList }

    fun initContext(context: Context) {
        midpointCalculator = MidpointCalculator(context)
    }

    fun fetchGatheringPlace(gatheringId: String) {
        _isLoading.value = true

        db.collection("groups")
            .get()
            .addOnSuccessListener { groupsSnapshot ->
                for (groupDoc in groupsSnapshot.documents) {
                    groupDoc.reference.collection("gatherings")
                        .document(gatheringId)
                        .get()
                        .addOnSuccessListener { gatheringDoc ->
                            if (gatheringDoc.exists()) {
                                @Suppress("UNCHECKED_CAST")
                                val memberPlaces = gatheringDoc.get("memberPlaces") as? List<Map<String, Any>>

                                // memberPlaces에서 coordinates 리스트 생성
                                val coordinates = mutableListOf<Coordinate>()

                                // 현재 사용자의 좌표 추가
                                _addressItem.value?.let { address ->
                                    coordinates.add(Coordinate(
                                        lat = address.latitude.toString(),
                                        lon = address.longitude.toString()
                                    ))
                                }

                                // memberPlaces의 좌표들 추가
                                memberPlaces?.forEach { place ->
                                    val lat = (place["latitude"] as? Number)?.toString()
                                    val lng = (place["longitude"] as? Number)?.toString()
                                    if (lat != null && lng != null) {
                                        coordinates.add(Coordinate(lat = lat, lon = lng))
                                    }
                                }

                                if (coordinates.size < 2) {
                                    _error.value = "최소 2개 이상의 위치가 필요합니다"
                                    _isLoading.value = false
                                    return@addOnSuccessListener
                                }

                                // 중간 지점 계산
                                viewModelScope.launch {
                                    val midpoint = midpointCalculator.findBestStation(coordinates)
                                    if (midpoint != null) {
                                        // ... 결과 처리 ...
                                    } else {
                                        _error.value = "중간 지점을 찾을 수 없습니다"
                                    }
                                    _isLoading.value = false
                                }
                            }
                        }
                }
            }
    }

    private fun calculateMidpoint(
        members: ArrayList<MemberResponse>,
        memberNum: Int,
        userPlaceName: String?,
        startPlace: PlaceLocationResponse?,
        gatheringId: String
    ) {
        viewModelScope.launch {
            try {
                val coordinates = mutableListOf<Coordinate>()

                // 현재 사용자의 좌표 추가
                _addressItem.value?.let { address ->
                    coordinates.add(Coordinate(
                        lat = address.latitude.toString(),
                        lon = address.longitude.toString()
                    ))
                } ?: startPlace?.let { place ->
                    coordinates.add(Coordinate(
                        lat = place.latitude,
                        lon = place.longitude
                    ))
                }

                if (coordinates.isEmpty()) {
                    _error.value = "출발 장소를 입력해주세요"
                    _isLoading.value = false
                    return@launch
                }

                val bestStation = midpointCalculator.findBestStation(coordinates)
                if (bestStation != null) {
                    val recommendedPlaces = ArrayList<RecommendPlaceResponse>().apply {
                        add(RecommendPlaceResponse(
                            placeName = bestStation.station,
                            address = "${bestStation.line} ${bestStation.station}역",
                            subwayTime = "${bestStation.maxTime}분 소요",
                            latitude = bestStation.coordinates.lat,
                            longitude = bestStation.coordinates.lon
                        ))
                    }

                    _response.postValue(
                        GatheringPlaceResponse(
                            memberList = members,
                            memberNum = memberNum,
                            userPlaceName = _addressItem.value?.title ?: userPlaceName,
                            startPlace = _addressItem.value?.let {
                                PlaceLocationResponse(
                                    it.latitude.toString(),
                                    it.longitude.toString()
                                )
                            } ?: startPlace,
                            placeList = recommendedPlaces
                        )
                    )
                } else {
                    _error.value = "중간 지점을 찾을 수 없습니다"
                }
            } catch (e: Exception) {
                _error.value = "중간 지점 계산에 실패했습니다"
                Log.e("GatheringPlaceViewModel", "Error calculating midpoint", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateAddressItem(newAddress: AddressItem?) {
        _addressItem.value = newAddress
    }

    fun saveSelectedPlace(gatheringId: String, place: Map<String, Any>, callback: (Boolean) -> Unit) {
        db.collection("groups")
            .get()
            .addOnSuccessListener { groupsSnapshot ->
                for (groupDoc in groupsSnapshot.documents) {
                    groupDoc.reference.collection("gatherings")
                        .document(gatheringId)
                        .update("place", place)
                        .addOnSuccessListener {
                            callback(true)
                        }
                        .addOnFailureListener { e ->
                            Log.e("GatheringPlaceViewModel", "Error saving place", e)
                            _error.value = "중간 지점 저장에 실패했습니다"
                            callback(false)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("GatheringPlaceViewModel", "Error fetching groups", e)
                _error.value = "중간 지점 저장에 실패했습니다"
                callback(false)
            }
    }

    fun addTestMemberPlaces(groupId: String, gatheringId: String) {
        _isLoading.value = true

        val memberPlaces = arrayListOf(
            hashMapOf(
                "userId" to "test_user_1",
                "latitude" to 37.5665,
                "longitude" to 126.9780,
                "name" to "서울역"
            ),
            hashMapOf(
                "userId" to "test_user_2",
                "latitude" to 37.5209,
                "longitude" to 127.1232,
                "name" to "잠실역"
            )
        )

        db.collection("groups")
            .document(groupId)
            .collection("gatherings")
            .document(gatheringId)
            .update("memberPlaces", memberPlaces)
            .addOnSuccessListener {
                _isLoading.value = false
                _error.value = "테스트 데이터가 추가되었습니다"
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = "테스트 데이터 추가 실패: ${e.message}"
                Log.e("GatheringPlaceViewModel", "Error adding test data", e)
            }
    }


}