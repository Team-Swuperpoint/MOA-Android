package com.swuperpoint.moa_android.viewmodel.main.group

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.content.Context
import android.util.Log
import com.swuperpoint.moa_android.data.remote.model.group.GatheringInfoResponse
import com.swuperpoint.moa_android.data.remote.model.group.PlaceLocationResponse
import com.swuperpoint.moa_android.util.Coordinate
import com.swuperpoint.moa_android.util.MidpointCalculator
import com.swuperpoint.moa_android.util.StationEvaluation
import kotlinx.coroutines.launch

class GatheringInfoViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val _response = MutableLiveData<GatheringInfoResponse>()
    val response: LiveData<GatheringInfoResponse> get() = _response

    private lateinit var midpointCalculator: MidpointCalculator

    // LiveData mappings...
    var gatheringName: LiveData<String> = _response.map { it.gatheringName }
    var gatheringDate: LiveData<String> = _response.map { it.date }
    var gatheringStartTime: LiveData<String> = _response.map { it.gatheringStartTime }
    var gatheringEndTime: LiveData<String> = _response.map { it.gatheringEndTime }
    var placeName: LiveData<String?> = _response.map { it.placeName }
    var subwayTime: LiveData<String?> = _response.map { it.subwayTime }
    var startPlace: LiveData<PlaceLocationResponse?> = _response.map { it.startPlace }
    var gatheringPlace: LiveData<PlaceLocationResponse?> = _response.map { it.gatheringPlace }

    // Context 초기화를 위한 함수
    fun initContext(context: Context) {
        midpointCalculator = MidpointCalculator(context)
    }

    // groupId 파라미터 추가
    fun fetchGatheringInfo(groupId: String, gatheringId: String) {
        Log.d("GatheringInfo", "Fetching info for gathering: $gatheringId in group: $groupId")

        db.collection("groups")
            .document(groupId)
            .collection("gatherings")
            .document(gatheringId)
            .get()
            .addOnSuccessListener { gatheringDoc ->
                if (gatheringDoc.exists()) {
                    // 중간 지점 위치 정보 가져오기
                    val placeMap = gatheringDoc.get("gatheringPlace") as? Map<String, Any>

                    val gatheringPlace = if (placeMap != null) {
                        PlaceLocationResponse(
                            latitude = placeMap["latitude"].toString(),
                            longitude = placeMap["longitude"].toString()
                        )
                    } else null

                    val gatheringInfo = GatheringInfoResponse(
                        gatheringId = gatheringId,
                        gatheringName = gatheringDoc.getString("gatheringName") ?: "",
                        date = gatheringDoc.getString("date") ?: "",
                        gatheringStartTime = gatheringDoc.getString("gatheringStartTime") ?: "",  // 필드명 수정
                        gatheringEndTime = gatheringDoc.getString("gatheringEndTime") ?: "",      // 필드명 수정
                        placeName = placeMap?.get("placeName")?.toString(),
                        subwayTime = placeMap?.get("subwayTime")?.toString(),
                        startPlace = null,
                        gatheringPlace = gatheringPlace
                    )

                    Log.d("GatheringInfo", "Document data: ${gatheringDoc.data}")
                    Log.d("GatheringInfo", "Gathering Response: $gatheringInfo")

                    _response.value = gatheringInfo
                }
            }
            .addOnFailureListener { e ->
                Log.e("GatheringInfo", "Error fetching gathering info", e)
            }
    }

    fun calculateMidpoint(startCoordinates: List<Coordinate>) {
        viewModelScope.launch {
            try {
                val stations = midpointCalculator.findBestStation(startCoordinates)
                if (stations.isNotEmpty()) {
                    val bestStation = stations[0] // 첫 번째 역을 기본값으로
                    _response.value = _response.value?.copy(
                        placeName = bestStation.station,
                        subwayTime = "${bestStation.maxTime}분",
                        gatheringPlace = PlaceLocationResponse(
                            bestStation.coordinates.lat,
                            bestStation.coordinates.lon
                        )
                    )

                    // Firebase에 중간 지점 정보 업데이트
                    updateMidpointInFirebase(bestStation)
                }
            } catch (e: Exception) {
                Log.e("GatheringInfoViewModel", "Error calculating midpoint", e)
            }
        }
    }

    private fun updateMidpointInFirebase(station: StationEvaluation) {
        val gatheringId = _response.value?.gatheringId ?: return

        val placeData = hashMapOf(
            "name" to station.station,
            "latitude" to station.coordinates.lat,
            "longitude" to station.coordinates.lon,
            "subwayTime" to "${station.maxTime}분"
        )

        db.collection("groups")
            .get()
            .addOnSuccessListener { groupsSnapshot ->
                for (groupDoc in groupsSnapshot.documents) {
                    groupDoc.reference.collection("gatherings")
                        .document(gatheringId)
                        .update("place", placeData)
                        .addOnFailureListener { e ->
                            Log.e("GatheringInfoViewModel", "Error updating place data", e)
                        }
                }
            }
    }

    private fun formatDate(dateString: String): String {
        try {
            val dateParts = dateString.split("-")
            if (dateParts.size == 3) {
                return "${dateParts[1]}월 ${dateParts[2]}일"
            }
        } catch (e: Exception) {
            Log.e("GatheringInfoViewModel", "Error formatting date", e)
        }
        return dateString
    }
}