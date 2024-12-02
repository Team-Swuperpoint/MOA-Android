package com.swuperpoint.moa_android.viewmodel.main.home

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.profile.NidProfileCallback
import com.navercorp.nid.profile.data.NidProfileResponse
import com.swuperpoint.moa_android.R
import com.swuperpoint.moa_android.data.remote.model.home.HomeGroupListResponse
import com.swuperpoint.moa_android.data.remote.model.home.HomeResponse
import com.swuperpoint.moa_android.view.main.home.data.HomeGatheringItem
import com.swuperpoint.moa_android.view.main.home.data.HomeGroupItem
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar

/* 홈 화면 뷰 모델 */
class HomeViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val _homeResponse = MutableLiveData<HomeResponse>() // 내부 수정용 변수
    val homeResponse: LiveData<HomeResponse> get() = _homeResponse // 외부 읽기 전용 변수

    // 닉네임
    var nickname: LiveData<String> = _homeResponse.map { it.nickname }

    // 모임 리스트
    var groupList = MutableLiveData<ArrayList<HomeGroupItem>>().apply {
        value = null
    }

    // 1개 모임 정보
    var gatheringInfo = MutableLiveData<HomeGatheringItem?>()

    init {
        // DTO -> Entity로 변환
        _homeResponse.observeForever { response ->
            val items = response.groupList?.mapIndexed { index, group ->
                HomeGroupItem(
                    groupId = group.groupId,
                    groupName = group.groupName,
                    color = group.bgColor,
                    emoji = group.emoji,
                    date = group.date,
                    isSelected = index == 0 // 첫번째 아이템만 true로 설정
                )
            } ?: emptyList()

            val gathering = response.groupInfo?.let {
                HomeGatheringItem(
                    gatheringId = it.gatheringId,
                    groupName = it.groupName,
                    gatheringName = it.gatheringName,
                    date = it.date,
                    location = it.location,
                    dDay = it.dDay,
                    memberProfileList = it.memberProfileList
                )
            }

            groupList.value = ArrayList(items)
            gatheringInfo.value = gathering
        }
        // 초기 데이터 로드
        fetchHomeData()
    }

    private fun getRelativeTimeString(dateString: String): String {
        try {
            val date = LocalDate.parse(dateString)
            val today = LocalDate.now()
            val daysUntil = ChronoUnit.DAYS.between(today, date)  // 날짜 계산 순서 변경

            return when {
                daysUntil == 0L -> "오늘"
                daysUntil > 0 -> "${daysUntil}일 후"
                else -> "지난 모임"  // 과거 모임인 경우
            }
        } catch (e: Exception) {
            return "날짜 정보 없음"
        }
    }

    public fun fetchHomeData() {
        // 네이버 로그인 SDK에서 현재 로그인된 계정의 이메일 가져오기
        NidOAuthLogin().callProfileApi(object : NidProfileCallback<NidProfileResponse> {
            override fun onSuccess(result: NidProfileResponse) {
                val email = result.profile?.email
                if (email != null) {
                    // users 컬렉션에서 해당 이메일을 document ID로 가진 문서 조회
                    db.collection("users")
                        .document(email)
                        .get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                val nickname = document.getString("nickname")
                                fetchGroupsWithUserName(nickname ?: "사용자")
                            }
                        }
                }
            }

            override fun onFailure(httpStatus: Int, message: String) {
                fetchGroupsWithUserName("사용자")
            }

            override fun onError(errorCode: Int, message: String) {
                fetchGroupsWithUserName("사용자")
            }
        })
    }

    private fun fetchGroupsWithUserName(userName: String) {
        Log.d("HomeViewModel", "Fetching groups with username: $userName")

        db.collection("groups")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("HomeViewModel", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val groupsList = mutableListOf<Pair<HomeGroupListResponse, LocalDate>>()
                    var processedGroups = 0
                    val totalGroups = snapshot.documents.size

                    snapshot.documents.forEach { document ->
                        try {
                            document.reference.collection("gatherings")
                                .whereGreaterThan("date", LocalDate.now().toString())  // 미래의 모임만
                                .orderBy("date", Query.Direction.ASCENDING)  // 날짜 오름차순
                                .limit(1)  // 가장 가까운 미래 모임
                                .get()
                                .addOnSuccessListener { gatheringDocs ->
                                    if (!gatheringDocs.isEmpty) {
                                        val dateString = gatheringDocs.documents[0].getString("date") ?: return@addOnSuccessListener
                                        val date = LocalDate.parse(dateString)

                                        val group = HomeGroupListResponse(
                                            groupId = document.id,
                                            groupName = document.getString("groupName") ?: "",
                                            bgColor = (document.get("bgColor") as? Long)?.toInt() ?: 0,
                                            emoji = document.getString("emoji") ?: "",
                                            date = getRelativeTimeString(dateString)
                                        )
                                        groupsList.add(Pair(group, date))
                                    }

                                    processedGroups++

                                    if (processedGroups == totalGroups) {
                                        // 날짜순으로 정렬
                                        val sortedGroups = groupsList
                                            .sortedBy { it.second }  // 날짜로 정렬
                                            .map { it.first }  // HomeGroupListResponse만 추출

                                        Log.d("HomeViewModel", "Posting HomeResponse with username: $userName")
                                        _homeResponse.postValue(HomeResponse(userName, ArrayList(sortedGroups), null))

                                        // 첫 번째 그룹의 모임 정보 가져오기
                                        if (sortedGroups.isNotEmpty()) {
                                            fetchUpcomingGathering(sortedGroups[0].groupId, sortedGroups[0].groupName)
                                        }
                                    }
                                }
                        } catch (e: Exception) {
                            Log.e("HomeViewModel", "Error processing group document", e)
                            processedGroups++
                        }
                    }
                }
            }
    }

    private fun fetchUpcomingGathering(groupId: String, groupName: String) {
        val today = LocalDate.now().toString()
        db.collection("groups").document(groupId)
            .collection("gatherings")
            .whereGreaterThanOrEqualTo("date", today)  // 오늘 포함 이후 모임
            .orderBy("date", Query.Direction.ASCENDING)  // 가까운 날짜순 정렬
            .limit(1)  // 가장 가까운 모임 1개
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val doc = documents.documents[0]
                    fetchMemberProfiles(groupId) { profileList ->
                        val dateStr = doc.getString("date") ?: ""
                        val dDayValue = if (dateStr.isNotEmpty()) {
                            val gatheringDate = LocalDate.parse(dateStr)
                            ChronoUnit.DAYS.between(LocalDate.now(), gatheringDate).toInt()
                        } else 0

                        val gathering = HomeGatheringItem(
                            gatheringId = doc.id.hashCode().toLong(),
                            groupName = groupName,
                            gatheringName = doc.getString("gatheringName") ?: "",
                            date = formatDate(
                                doc.getString("date") ?: "",
                                doc.getString("time") ?: "00:00"  // time 필드 가져오기
                            ),
                            location = doc.getString("location") ?: "",
                            dDay = dDayValue,
                            memberProfileList = profileList
                        )
                        gatheringInfo.postValue(gathering)
                    }
                } else {
                    gatheringInfo.postValue(null)
                }
            }
    }

    private fun fetchMemberProfiles(groupId: String, callback: (ArrayList<String>) -> Unit) {
        db.collection("groups").document(groupId)
            .collection("members")
            .get()
            .addOnSuccessListener { documents ->
                val profileList = ArrayList<String>()
                documents.forEach { doc ->
                    doc.getString("profile")?.let { profile ->
                        profileList.add(profile)
                    }
                }
                callback(profileList)
            }
            .addOnFailureListener { e ->
                Log.e("HomeViewModel", "Error fetching member profiles", e)
                callback(ArrayList())
            }
    }

    private fun calculateDDay(dateString: String): Int {
        // dateString을 파싱하여 D-Day 계산 로직 구현
        return 0 // 임시 반환값
    }

    private fun formatDate(dateStr: String, timeStr: String): String {
        val date = LocalDate.parse(dateStr)
        val time = LocalTime.parse(timeStr)

        val dayOfWeek = when (date.dayOfWeek) {
            DayOfWeek.MONDAY -> "월"
            DayOfWeek.TUESDAY -> "화"
            DayOfWeek.WEDNESDAY -> "수"
            DayOfWeek.THURSDAY -> "목"
            DayOfWeek.FRIDAY -> "금"
            DayOfWeek.SATURDAY -> "토"
            DayOfWeek.SUNDAY -> "일"
        }

        return "${date.monthValue}월 ${date.dayOfMonth}일 (${dayOfWeek}) ${time.format(
            DateTimeFormatter.ofPattern("HH:mm"))}"
    }

    private fun updateHomeResponse(groupsList: ArrayList<HomeGroupListResponse>) {
        val currentResponse = _homeResponse.value ?: HomeResponse("사용자", groupsList, null)
        _homeResponse.postValue(currentResponse.copy(groupList = groupsList))
    }


//    // 더미데이터 적용
//    // TODO: 파이어베이스 연결 시 삭제하기
//    fun fetchGroupList(): ArrayList<HomeGroupListResponse> {
//        return arrayListOf(
//            HomeGroupListResponse(R.color.main_500, "🍔", "9일"),
//            HomeGroupListResponse(R.color.main_100, "🐶", "13일"),
//            HomeGroupListResponse(R.color.sub_500, "✈️", "24일")
//        )
//    }

//    fun fetchGatheringInfo(): HomeGatheringItem {
//        return HomeGatheringItem(
//                1,
//                "먹짱친구들",
//                "빵순이투어🥐",
//                "10월 8일 (화) 15:02",
//                "경복궁역 5번 출구",
//                2,
//                arrayListOf("https://search.pstatic.net/common/?src=http%3A%2F%2Fblogfiles.naver.net%2FMjAyNDA0MjhfMjY4%2FMDAxNzE0MzEyMjk5NDQ4.Q80gZpRJgqUFcOCVQJHZp8dHtO3R7tNyiDTuyl3jC7Ug.EleTj7FhgQRu4tYOT9lYvx2Frx2dHZO3CPsJV9qspUwg.JPEG%2FScreenshot%25A3%25DF20240428%25A3%25DF224935%25A3%25DFInstagram.jpg&type=sc960_832",
//                    "https://search.pstatic.net/sunny/?src=https%3A%2F%2Fi1.sndcdn.com%2Fartworks-N58pyyEi9rcBd4wf-STMfCQ-t500x500.jpg&type=sc960_832",
//                    "https://search.pstatic.net/common/?src=http%3A%2F%2Fblogfiles.naver.net%2FMjAyMjAxMzFfMjg5%2FMDAxNjQzNjM4NzU1NTA5.dhUbIVx5-n9NmT38o1HncivKJAVMzHk3FCoRziAA9d4g.1pLdpIJV8KMxKqYsxtue18CpHeReRPoSV-1R2Ll_0E8g.JPEG.gyulibae0905%2FScreenshot%25A3%25DF20220131%25A3%25AD225304%25A3%25DFInstagram.jpg&type=sc960_832")
//        )
//    }

    // response 데이터 설정
    fun setHomeResponse(response: HomeResponse) {
        _homeResponse.value = response
    }

    override fun onCleared() {
        super.onCleared()
        // 관찰 해제
        _homeResponse.removeObserver { }
    }

    // 다음 모임 확인하기
    fun showNextGathering() {
    }

    fun fetchSelectedGroupGathering(position: Int) {
        val selectedGroup = groupList.value?.get(position) ?: return
        // groupId로 직접 모임 정보 가져오기
        fetchUpcomingGathering(selectedGroup.groupId, selectedGroup.groupName)
    }
}