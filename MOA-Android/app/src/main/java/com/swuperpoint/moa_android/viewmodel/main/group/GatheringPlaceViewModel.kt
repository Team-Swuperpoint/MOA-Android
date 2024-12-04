package com.swuperpoint.moa_android.viewmodel.main.group

import androidx.lifecycle.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.content.Context
import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.swuperpoint.moa_android.data.remote.model.group.*
import com.swuperpoint.moa_android.view.main.group.data.*
import com.swuperpoint.moa_android.util.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class GatheringPlaceViewModel : ViewModel() {
    private val db = Firebase.firestore
    private lateinit var midpointCalculator: MidpointCalculator

    private val _response = MutableLiveData<GatheringPlaceResponse>()
    val response: LiveData<GatheringPlaceResponse> get() = _response

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isButtonEnabled = MutableLiveData<Boolean>()
    val isButtonEnabled: LiveData<Boolean> = _isButtonEnabled

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

    private var groupId: String = ""

    fun setGroupId(id: String) {
        groupId = id
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

    fun updateAddressItem(newAddress: AddressItem?, groupId: String, gatheringId: String, userId: String) {
        _addressItem.value = newAddress
        newAddress?.let {
            saveUserLocation(groupId, gatheringId, userId, it)
        }
    }

    // 멤버 위치 입력 상태 체크 로직 개선
    private fun checkButtonState(memberPlaces: List<Map<String, Any>>) {
        _isButtonEnabled.value = false  // 초기값 false

        if (memberPlaces.size == 2) {
            val allMembersHaveLocation = memberPlaces.all {
                it["latitude"] != null && it["longitude"] != null
            }
            // 에러 메시지 조건 수정
            if (!allMembersHaveLocation) {
                _error.value = "모든 멤버의 위치 입력이 필요합니다 (${memberPlaces.size}/2)"
            }
            _isButtonEnabled.value = allMembersHaveLocation
        }
    }
    private val _progressCount = MutableLiveData<String>()
    val progressCount: LiveData<String> = _progressCount

    private fun updateProgress(memberPlaces: List<Map<String, Any>>, totalMembers: Int) {
        _progressCount.value = "${memberPlaces.size}/$totalMembers"
    }

    // ViewModel
    private fun fetchGatheringDocument(groupId: String, gatheringId: String) {
        viewModelScope.launch {
            try {
                val gatheringDoc = db.collection("groups")
                    .document(groupId)
                    .collection("gatherings")
                    .document(gatheringId)
                    .get()
                    .await()

                Log.d("GatheringPlace", "Doc data: ${gatheringDoc.data}")
                val memberUIDs = gatheringDoc.get("memberUIDs") as? List<String> ?: listOf()
                Log.d("GatheringPlace", "memberUIDs: $memberUIDs")
                val memberPlaces = gatheringDoc.get("memberPlaces") as? List<Map<String, Any>> ?: listOf()
                Log.d("GatheringPlace", "memberPlaces: $memberPlaces")

                val groupDoc = db.collection("groups").document(groupId).get().await()
                val totalMembers = groupDoc.getLong("groupMemberNum") ?: 0

                _progressCount.value = "${memberPlaces?.size}/$totalMembers"
                _isButtonEnabled.value = memberPlaces.size == memberUIDs.size && memberUIDs.size >= 2
            } catch(e: Exception) {
                _error.value = "모임 정보를 가져오는 데 실패했습니다"
            }
        }
    }

    fun fetchGatheringPlace(groupId: String, gatheringId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // 1. groups 컬렉션에서 전체 멤버 수(memberUIDs) 가져오기
                val groupDoc = db.collection("groups")
                    .document(groupId)
                    .get()
                    .await()

                val totalMembers = (groupDoc.get("memberUIDs") as? List<String>)?.size ?: 0

                // 2. gathering 문서에서 memberPlaces 가져오기
                val gatheringDoc = db.collection("groups")
                    .document(groupId)
                    .collection("gatherings")
                    .document(gatheringId)
                    .get()
                    .await()

                val memberPlaces = gatheringDoc.get("memberPlaces") as? List<Map<String, Any>> ?: listOf()

                // 3. 현재 사용자의 위치 정보 찾기
                val currentUserId = Firebase.auth.currentUser?.uid
                val userPlace = memberPlaces.find { it["userId"] == currentUserId }

                // 4. Response 업데이트
                val memberList = fetchUserDetails(memberPlaces)
                _response.postValue(
                    GatheringPlaceResponse(
                        memberList = memberList,
                        memberNum = memberPlaces.size,
                        userPlaceName = userPlace?.get("name") as? String,
                        startPlace = userPlace?.let {
                            PlaceLocationResponse(
                                it["latitude"].toString(),
                                it["longitude"].toString()
                            )
                        },
                        placeList = _response.value?.placeList
                    )
                )

                // 5. 진행 상태 업데이트
                _progressCount.value = "${memberPlaces.size}/$totalMembers"
                _isButtonEnabled.value = memberPlaces.size == totalMembers && totalMembers >= 2

                // 6. 조건 충족시 중간지점 계산
                if (_isButtonEnabled.value == true) {
                    calculateMidpoint(memberPlaces)
                }

            } catch(e: Exception) {
                _error.value = "모임 정보를 가져오는 데 실패했습니다"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 계산 중 메시지를 위한 LiveData
    private val _calculatingMessage = MutableLiveData<String?>()
    val calculatingMessage: LiveData<String?> = _calculatingMessage


    private suspend fun calculateMidpoint(memberPlaces: List<Map<String, Any>>) {
        try {
            _calculatingMessage.postValue("중간 지점 계산 중...")

            val coordinates = mutableListOf<Coordinate>()
            memberPlaces.forEach { place ->
                val lat = (place["latitude"] as? Number)?.toString()
                val lng = (place["longitude"] as? Number)?.toString()
                if (lat != null && lng != null) {
                    coordinates.add(Coordinate(lat = lat, lon = lng))
                }
            }


            val stations = midpointCalculator.findBestStation(coordinates)
            if (stations.isNotEmpty()) {
                val recommendedPlaces = ArrayList<RecommendPlaceResponse>().apply {
                    stations.forEach { station ->
                        add(RecommendPlaceResponse(
                            placeName = station.station,
                            address = "${station.line} ${station.station}역",
                            subwayTime = "${station.maxTime}분",
                            latitude = station.coordinates.lat,
                            longitude = station.coordinates.lon
                        ))
                    }
                }


                val memberList = fetchUserDetails(memberPlaces)
                _response.postValue(
                    GatheringPlaceResponse(
                        memberList = memberList,
                        memberNum = memberPlaces.size,
                        userPlaceName = _addressItem.value?.title,
                        startPlace = _addressItem.value?.let {
                            PlaceLocationResponse(
                                it.latitude.toString(),
                                it.longitude.toString()
                            )
                        },
                        placeList = recommendedPlaces
                    )
                )
            } else {
                _error.value = "중간 지점을 찾을 수 없습니다"
            }
        } catch (e: Exception) {
            _error.value = "중간 지점 계산에 실패했습니다: ${e.message}"
            Log.e("GatheringPlaceViewModel", "중간 지점 계산 중 오류 발생", e)
        } finally {
            _calculatingMessage.postValue(null)  // 계산 완료되면 메시지 제거
        }
    }

    fun findHotplaceStation(coordinates: List<Coordinate>) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                _calculatingMessage.value = "핫플레이스 찾는 중..."
                val stations = midpointCalculator.findHotplaceStation(coordinates)
                if (stations.isNotEmpty()) {
                    val recommendedPlaces = ArrayList<RecommendPlaceResponse>().apply {
                        stations.forEach { station ->
                            add(RecommendPlaceResponse(
                                placeName = station.station,
                                address = "${station.line} ${station.station}역",
                                subwayTime = "${station.maxTime}분",
                                latitude = station.coordinates.lat,
                                longitude = station.coordinates.lon
                            ))
                        }
                    }

                    // 기존 response 객체를 사용하여 UI 업데이트
                    _response.value = _response.value?.copy(
                        placeList = recommendedPlaces
                    )
                } else {
                    _error.value = "주변에 핫플레이스를 찾을 수 없습니다"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _calculatingMessage.value = null  // 계산 완료되면 메시지 제거
            }
        }
    }

    // saveSelectedPlace 추가
    fun saveSelectedPlace(gatheringId: String, place: RecommendPlaceResponse, callback: (Boolean) -> Unit) {
        val placeMap = hashMapOf(
            "latitude" to place.latitude.toDouble(),
            "longitude" to place.longitude.toDouble(),
            "placeName" to place.placeName,
            "address" to place.address,
            "subwayTime" to place.subwayTime
        )

        db.collection("groups")
            .document(groupId)
            .collection("gatherings")
            .document(gatheringId)
            .update("gatheringPlace", placeMap)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { e ->
                _error.value = "중간 지점 저장에 실패했습니다"
                callback(false)
            }
    }

    fun saveUserLocation(groupId: String, gatheringId: String, userId: String, location: AddressItem) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { userDoc ->
                val nickname = userDoc.getString("nickname") ?: "Unknown"
                val profileUrl = userDoc.getString("profile") ?: ""

                val memberPlace = hashMapOf(
                    "userId" to userId,
                    "latitude" to location.latitude.toDouble(),
                    "longitude" to location.longitude.toDouble(),
                    "name" to location.title,
                    "nickname" to nickname,
                    "profileUrl" to profileUrl
                ) as Map<String, Any>

                updateGatheringWithLocation(groupId, gatheringId, userId, memberPlace)
            }
            .addOnFailureListener { e ->
                _error.value = "사용자 정보를 가져오는데 실패했습니다"
                Log.e("GatheringPlaceViewModel", "사용자 정보 조회 실패", e)
            }
    }

    private fun updateGatheringWithLocation(groupId: String, gatheringId: String, userId: String, memberPlace: Map<String, Any>) {
        db.collection("groups")
            .document(groupId)
            .collection("gatherings")
            .document(gatheringId)
            .get()
            .addOnSuccessListener { gatheringDoc ->
                val currentPlaces = gatheringDoc.get("memberPlaces") as? MutableList<Map<String, Any>> ?: mutableListOf()

                val existingIndex = currentPlaces.indexOfFirst { it["userId"] == userId }
                if (existingIndex != -1) {
                    currentPlaces[existingIndex] = memberPlace
                } else {
                    currentPlaces.add(memberPlace)
                }

                checkButtonState(currentPlaces)

                gatheringDoc.reference.update("memberPlaces", currentPlaces)
                    .addOnSuccessListener {
                        fetchGatheringPlace(groupId, gatheringId)
                    }
                    .addOnFailureListener { e ->
                        _error.value = "위치 저장에 실패했습니다"
                        Log.e("GatheringPlaceViewModel", "위치 저장 실패", e)
                    }
            }
    }

    suspend fun fetchUserDetails(memberPlaces: List<Map<String, Any>>): ArrayList<MemberResponse> {
        val memberList = ArrayList<MemberResponse>()

        memberPlaces.forEach { place ->
            try {
                val userId = place["userId"].toString()
                val userDoc = db.collection("users")
                    .document(userId)
                    .get()
                    .await()

                val member = MemberResponse(
                    memberId = userId,
                    profileImgURL = userDoc.getString("profile") ?: "",
                    memberName = userDoc.getString("nickname") ?: "Unknown"
                )
                memberList.add(member)
            } catch (e: Exception) {
                Log.e("GatheringPlaceViewModel", "Error fetching user details", e)
            }
        }
        return memberList
    }
}