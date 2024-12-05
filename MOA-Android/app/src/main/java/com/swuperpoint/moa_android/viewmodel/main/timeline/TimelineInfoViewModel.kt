package com.swuperpoint.moa_android.viewmodel.main.timeline

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.google.firebase.firestore.FirebaseFirestore
import com.swuperpoint.moa_android.data.remote.model.timeline.TimelineInfoResponse
import com.swuperpoint.moa_android.data.remote.model.timeline.TimelinePhotoResponse

class TimelineInfoViewModel: ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _response = MutableLiveData<TimelineInfoResponse>()
    val response: LiveData<TimelineInfoResponse> get() = _response

    // LiveData 변환
    val gatheringName: LiveData<String> = _response.map { it.gatheringName }
    val date: LiveData<String> = _response.map { it.date }
    val placeName: LiveData<String> = _response.map { it.placeName }
    val groupName: LiveData<String> = _response.map { it.groupName }
    val timelineList: LiveData<ArrayList<TimelinePhotoResponse>> = _response.map { it.timelineList }

    private var groupId: String = ""
    private var gatheringId: String = ""

    fun setIds(groupId: String, gatheringId: String) {
        this.groupId = groupId
        this.gatheringId = gatheringId
    }

    // 타임라인 상세 정보 조회
    fun fetchTimelineInfo(timelineId: String) {
        db.collection("groups").get()
            .addOnSuccessListener { groupsSnapshot ->
                for (groupDoc in groupsSnapshot.documents) {
                    groupDoc.reference.collection("gatherings").get()
                        .addOnSuccessListener { gatheringsSnapshot ->
                            for (gatheringDoc in gatheringsSnapshot.documents) {
                                gatheringDoc.reference.collection("timelines")
                                    .document(timelineId)
                                    .get()
                                    .addOnSuccessListener { timelineDoc ->
                                        if (timelineDoc.exists()) {
                                            // ID 저장
                                            groupId = groupDoc.id
                                            gatheringId = gatheringDoc.id

                                            // 사진 목록 변환
                                            val photoList = ArrayList((timelineDoc.get("photos") as? List<Map<String, Any>>)?.map { photo ->
                                                // 위도, 경도 값 추출
                                                val latitude = (photo["latitude"] as? Number)?.toDouble()
                                                val longitude = (photo["longitude"] as? Number)?.toDouble()

                                                TimelinePhotoResponse(
                                                    time = photo["time"] as? String ?: "",
                                                    latitude = latitude,
                                                    longitude = longitude,
                                                    photoList = ArrayList(photo["photoList"] as? List<String> ?: listOf())
                                                )
                                            } ?: listOf())

                                            // 타임라인 정보 구성
                                            val timelineInfo = TimelineInfoResponse(
                                                gatheringName = timelineDoc.getString("gatheringName") ?: "",
                                                date = timelineDoc.getString("date") ?: "",
                                                placeName = timelineDoc.getString("placeName") ?: "",
                                                groupName = timelineDoc.getString("groupName") ?: "",
                                                timelineList = photoList
                                            )

                                            _response.postValue(timelineInfo)
                                        }
                                    }
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Timeline", "타임라인 정보 조회 중 오류 발생", e)
            }
    }

    // 타임라인 삭제
    fun deleteTimeline(timelineId: String, onSuccess: () -> Unit) {
        if (groupId.isEmpty() || gatheringId.isEmpty()) {
            return
        }

        db.collection("groups")
            .document(groupId)
            .collection("gatherings")
            .document(gatheringId)
            .collection("timelines")
            .document(timelineId)
            .delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("Timeline", "타임라인 삭제 실패", e)
            }
    }
}