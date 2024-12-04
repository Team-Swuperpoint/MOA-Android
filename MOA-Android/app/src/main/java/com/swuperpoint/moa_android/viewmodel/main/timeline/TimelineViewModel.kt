package com.swuperpoint.moa_android.viewmodel.main.timeline

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.swuperpoint.moa_android.data.remote.model.timeline.TimelineResponse

/* 타임라인 화면의 뷰모델 */
class TimelineViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val _response = MutableLiveData<ArrayList<TimelineResponse>>()
    val response: LiveData<ArrayList<TimelineResponse>> get() = _response

    // 타임라인 리스트
    var timelineList: LiveData<ArrayList<TimelineResponse>?> = _response.map { it }

    fun getCurrentUserId(): String = auth.currentUser?.uid ?: ""

    fun fetchTimeline() {
        val currentUserId = getCurrentUserId()
        val allTimelines = ArrayList<TimelineResponse>()

        db.collection("groups")
            .whereArrayContains("members", currentUserId)
            .get()
            .addOnSuccessListener { groupSnapshot ->
                var completedGroups = 0
                val totalGroups = groupSnapshot.size()

                for (groupDoc in groupSnapshot.documents) {
                    groupDoc.reference.collection("gatherings").get()
                        .addOnSuccessListener { gatheringSnapshot ->
                            var completedGatherings = 0
                            val totalGatherings = gatheringSnapshot.size()

                            if (gatheringSnapshot.isEmpty) {
                                completedGroups++
                                if (completedGroups == totalGroups) {
                                    finishTimelines(allTimelines)
                                }
                                return@addOnSuccessListener
                            }

                            // 각 모임의 타임라인 조회
                            for (gatheringDoc in gatheringSnapshot.documents) {
                                gatheringDoc.reference.collection("timelines")
                                    .orderBy("createdAt", Query.Direction.DESCENDING)
                                    .get()
                                    .addOnSuccessListener { timelineSnapshot ->
                                        for (doc in timelineSnapshot.documents) {
                                            try {
                                                Log.d("Timeline", "Processing timeline: ${doc.id}, createdAt: ${doc.getTimestamp("createdAt")}")
                                                val timeline = TimelineResponse(
                                                    timelineId = doc.id,
                                                    date = doc.getString("date") ?: "",
                                                    placeName = doc.getString("placeName") ?: "",  // root level placeName 사용
                                                    groupId = groupDoc.id,
                                                    groupName = doc.getString("groupName") ?: "",
                                                    gatheringName = doc.getString("gatheringName") ?: "",
                                                    groupMemberNum = doc.getLong("groupMemberNum")?.toInt() ?: 0,
                                                    gatheringImgURL = doc.getString("gatheringImgURL") ?: "",
                                                    createdAt = doc.getTimestamp("createdAt"),
                                                    createdBy = doc.getString("createdBy") ?: ""
                                                )

                                                if (timeline.createdBy == currentUserId) {
                                                    allTimelines.add(timeline)
                                                }
                                                allTimelines.add(timeline)
                                            } catch (e: Exception) {
                                                Log.e("Timeline", "타임라인 데이터 처리 중 오류 발생: ${doc.id}", e)
                                            }
                                        }

                                        // 모든 데이터 조회 완료 체크
                                        completedGatherings++
                                        if (completedGatherings == totalGatherings) {
                                            completedGroups++
                                            if (completedGroups == totalGroups) {
                                                finishTimelines(allTimelines)
                                            }
                                        }
                                    }
                            }
                        }
                }
            }
    }

    private fun finishTimelines(timelines: ArrayList<TimelineResponse>) {
        // createdAt 타임스탬프로 정렬
        timelines.sortByDescending { it.createdAt }
        _response.postValue(timelines)
    }
}