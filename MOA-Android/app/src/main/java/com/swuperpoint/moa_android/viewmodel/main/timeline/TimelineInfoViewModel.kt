package com.swuperpoint.moa_android.viewmodel.main.timeline

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.swuperpoint.moa_android.data.remote.model.timeline.TimelineInfoResponse
import com.swuperpoint.moa_android.data.remote.model.timeline.TimelinePhotoResponse

/* 타임라인 정보 화면의 뷰모델 */
class TimelineInfoViewModel: ViewModel() {
    private val _response = MutableLiveData<TimelineInfoResponse>()
    val response: LiveData<TimelineInfoResponse> get() = _response

    val gatheringName: LiveData<String> = _response.map { it.gatheringName } // 모임 이름
    val date: LiveData<String> = _response.map { it.date } // 모임 날짜
    val placeName: LiveData<String> = _response.map { it.placeName } // 모임 중간 지점 장소
    val groupName: LiveData<String> = _response.map { it.groupName } // 그룹 이름
    val timelineList: LiveData<ArrayList<TimelinePhotoResponse>> = _response.map { it.timelineList } // 타임라인 사진 리스트

    // TODO: timelineId로 파이어베이스에서 데이터 검색
    fun fetchTimelineInfo(timelineId: String) {
        val dummyResponse = TimelineInfoResponse(
            gatheringName = "빵순이투어",
            date = "2024.10.18 (수)",
            placeName = "북촌",
            groupName = "먹짱친구들",
            timelineList = arrayListOf(
                TimelinePhotoResponse(
                    time = "오전 8시 19분",
                    placeName = "북촌",
                    photoList = arrayListOf(
                        "https://scontent.cdninstagram.com/v/t51.29350-15/459785497_563054136150695_1506853460429416071_n.jpg?stp=dst-jpg_e35&efg=eyJ2ZW5jb2RlX3RhZyI6ImltYWdlX3VybGdlbi4xNDQweDE4MDAuc2RyLmYyOTM1MC5kZWZhdWx0X2ltYWdlIn0&_nc_ht=scontent.cdninstagram.com&_nc_cat=111&_nc_ohc=tbcDYa8cFSMQ7kNvgEWprJI&_nc_gid=7d5d161ae3ae485ba64b4324ca13fa99&edm=APs17CUBAAAA&ccb=7-5&ig_cache_key=MzQ1Njg0NDMxNDc0NjU1MDQ3NQ%3D%3D.3-ccb7-5&oh=00_AYDMkrETwDkmlTyPBURJbSpvDaB68lA7CMcqWTdIWsa9zw&oe=674B33C6&_nc_sid=10d13b",
                        "https://scontent-ssn1-1.cdninstagram.com/v/t51.29350-15/401092395_876814224109119_7386721604391564567_n.heic?stp=dst-jpg_e35_tt6&efg=eyJ2ZW5jb2RlX3RhZyI6ImltYWdlX3VybGdlbi4xMjAweDE1MDAuc2RyLmYyOTM1MC5kZWZhdWx0X2ltYWdlIn0&_nc_ht=scontent-ssn1-1.cdninstagram.com&_nc_cat=103&_nc_ohc=FDK2-Gu5j1MQ7kNvgGU4kvH&_nc_gid=9e3ccd67d1c94135b437ff95b6fc5556&edm=AOmX9WgBAAAA&ccb=7-5&ig_cache_key=MzIzNDM4MTYwODI3MDU3NzU0NA%3D%3D.3-ccb7-5&oh=00_AYB5OzEMNgvCblMaBVh3xEj_epZQgnarsyjWp2D-EeUpbQ&oe=674B1F70&_nc_sid=bfaa47",
                        "https://scontent-ssn1-1.cdninstagram.com/v/t51.29350-15/457364110_1727202384772753_502586513782673359_n.jpg?stp=dst-jpg_e35_tt6&efg=eyJ2ZW5jb2RlX3RhZyI6ImltYWdlX3VybGdlbi4xNDQweDE4MDAuc2RyLmYyOTM1MC5kZWZhdWx0X2ltYWdlIn0&_nc_ht=scontent-ssn1-1.cdninstagram.com&_nc_cat=101&_nc_ohc=SJG3PxnyNGgQ7kNvgFkAECV&_nc_gid=190fd06c5d4f4257891af4f620eb1f78&edm=AOmX9WgBAAAA&ccb=7-5&ig_cache_key=MzQ0NjY1MDcxMzAyNDA5MDI5NQ%3D%3D.3-ccb7-5&oh=00_AYAH5f-vQouhO4Bb5SI4XDBb1HIXDWJ5XhLKTVSQyoxctw&oe=674B2BFF&_nc_sid=bfaa47"
                    )
                ),
                TimelinePhotoResponse(
                    time = "오후 12시 56분",
                    placeName = "동촌",
                    photoList = arrayListOf(
                        "https://scontent-ssn1-1.cdninstagram.com/v/t51.29350-15/457364110_1727202384772753_502586513782673359_n.jpg?stp=dst-jpg_e35_tt6&efg=eyJ2ZW5jb2RlX3RhZyI6ImltYWdlX3VybGdlbi4xNDQweDE4MDAuc2RyLmYyOTM1MC5kZWZhdWx0X2ltYWdlIn0&_nc_ht=scontent-ssn1-1.cdninstagram.com&_nc_cat=101&_nc_ohc=SJG3PxnyNGgQ7kNvgFkAECV&_nc_gid=190fd06c5d4f4257891af4f620eb1f78&edm=AOmX9WgBAAAA&ccb=7-5&ig_cache_key=MzQ0NjY1MDcxMzAyNDA5MDI5NQ%3D%3D.3-ccb7-5&oh=00_AYAH5f-vQouhO4Bb5SI4XDBb1HIXDWJ5XhLKTVSQyoxctw&oe=674B2BFF&_nc_sid=bfaa47"
                    )
                ),
                TimelinePhotoResponse(
                    time = "오후 3시 08분",
                    placeName = "서촌",
                    photoList = arrayListOf(
                        "https://scontent-ssn1-1.cdninstagram.com/v/t51.29350-15/401092395_876814224109119_7386721604391564567_n.heic?stp=dst-jpg_e35_tt6&efg=eyJ2ZW5jb2RlX3RhZyI6ImltYWdlX3VybGdlbi4xMjAweDE1MDAuc2RyLmYyOTM1MC5kZWZhdWx0X2ltYWdlIn0&_nc_ht=scontent-ssn1-1.cdninstagram.com&_nc_cat=103&_nc_ohc=FDK2-Gu5j1MQ7kNvgGU4kvH&_nc_gid=9e3ccd67d1c94135b437ff95b6fc5556&edm=AOmX9WgBAAAA&ccb=7-5&ig_cache_key=MzIzNDM4MTYwODI3MDU3NzU0NA%3D%3D.3-ccb7-5&oh=00_AYB5OzEMNgvCblMaBVh3xEj_epZQgnarsyjWp2D-EeUpbQ&oe=674B1F70&_nc_sid=bfaa47",
                        "https://scontent-ssn1-1.cdninstagram.com/v/t51.29350-15/457364110_1727202384772753_502586513782673359_n.jpg?stp=dst-jpg_e35_tt6&efg=eyJ2ZW5jb2RlX3RhZyI6ImltYWdlX3VybGdlbi4xNDQweDE4MDAuc2RyLmYyOTM1MC5kZWZhdWx0X2ltYWdlIn0&_nc_ht=scontent-ssn1-1.cdninstagram.com&_nc_cat=101&_nc_ohc=SJG3PxnyNGgQ7kNvgFkAECV&_nc_gid=190fd06c5d4f4257891af4f620eb1f78&edm=AOmX9WgBAAAA&ccb=7-5&ig_cache_key=MzQ0NjY1MDcxMzAyNDA5MDI5NQ%3D%3D.3-ccb7-5&oh=00_AYAH5f-vQouhO4Bb5SI4XDBb1HIXDWJ5XhLKTVSQyoxctw&oe=674B2BFF&_nc_sid=bfaa47"
                    )
                ),
                TimelinePhotoResponse(
                    time = "오후 5시 24분",
                    placeName = "동촌",
                    photoList = arrayListOf(
                        "https://scontent-ssn1-1.cdninstagram.com/v/t51.29350-15/457364110_1727202384772753_502586513782673359_n.jpg?stp=dst-jpg_e35_tt6&efg=eyJ2ZW5jb2RlX3RhZyI6ImltYWdlX3VybGdlbi4xNDQweDE4MDAuc2RyLmYyOTM1MC5kZWZhdWx0X2ltYWdlIn0&_nc_ht=scontent-ssn1-1.cdninstagram.com&_nc_cat=101&_nc_ohc=SJG3PxnyNGgQ7kNvgFkAECV&_nc_gid=190fd06c5d4f4257891af4f620eb1f78&edm=AOmX9WgBAAAA&ccb=7-5&ig_cache_key=MzQ0NjY1MDcxMzAyNDA5MDI5NQ%3D%3D.3-ccb7-5&oh=00_AYAH5f-vQouhO4Bb5SI4XDBb1HIXDWJ5XhLKTVSQyoxctw&oe=674B2BFF&_nc_sid=bfaa47"
                    )
                ),
                TimelinePhotoResponse(
                    time = "오후 8시 34분",
                    placeName = "동촌",
                    photoList = arrayListOf(
                        "https://scontent-ssn1-1.cdninstagram.com/v/t51.29350-15/457364110_1727202384772753_502586513782673359_n.jpg?stp=dst-jpg_e35_tt6&efg=eyJ2ZW5jb2RlX3RhZyI6ImltYWdlX3VybGdlbi4xNDQweDE4MDAuc2RyLmYyOTM1MC5kZWZhdWx0X2ltYWdlIn0&_nc_ht=scontent-ssn1-1.cdninstagram.com&_nc_cat=101&_nc_ohc=SJG3PxnyNGgQ7kNvgFkAECV&_nc_gid=190fd06c5d4f4257891af4f620eb1f78&edm=AOmX9WgBAAAA&ccb=7-5&ig_cache_key=MzQ0NjY1MDcxMzAyNDA5MDI5NQ%3D%3D.3-ccb7-5&oh=00_AYAH5f-vQouhO4Bb5SI4XDBb1HIXDWJ5XhLKTVSQyoxctw&oe=674B2BFF&_nc_sid=bfaa47"
                    )
                ),
            )
        )
        // 데이터 업로드
        _response.value = dummyResponse
    }
}