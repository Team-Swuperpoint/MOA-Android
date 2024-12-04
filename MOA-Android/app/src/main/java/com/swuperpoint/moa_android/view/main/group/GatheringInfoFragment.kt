package com.swuperpoint.moa_android.view.main.group

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakao.vectormap.label.LabelOptions
import com.swuperpoint.moa_android.R
import com.swuperpoint.moa_android.data.remote.model.group.PlaceLocationResponse
import com.swuperpoint.moa_android.databinding.FragmentGatheringInfoBinding
import com.swuperpoint.moa_android.view.base.BaseFragment
import com.swuperpoint.moa_android.viewmodel.main.group.GatheringInfoViewModel

/* 모임 정보 화면 */
class GatheringInfoFragment : BaseFragment<FragmentGatheringInfoBinding>(FragmentGatheringInfoBinding::inflate) {
    private val viewModel: GatheringInfoViewModel by viewModels() // 뷰모델
    private var gatheringId: String = "" // 모임 id
    private var groupId: String = ""
    private var date: String = "" // 모임 날짜
    private var startTime: String = "" // 모임 시작 시간
    private var endTime: String = "" // 모임 종료 시간

    private var kakaoMap: KakaoMap? = null  // 카카오맵 객체 저장용
    private var pendingPlace: PlaceLocationResponse? = null  // 맵이 준비되기 전에 받은 장소 데이터를 저장


    override fun initViewCreated() {
        // 다른 화면에서 전달받은 모임id 설정
        val args: GatheringInfoFragmentArgs by navArgs()
        gatheringId = args.gatheringId
        groupId = args.groupId

        // 상태바 색상 변경
        changeStatusbarColor(R.color.white, isLightMode = true)

        // groupId도 같이 전달받아야 함
        val groupId = args.groupId

        // 데이터 로드 시 groupId도 함께 전달
        viewModel.fetchGatheringInfo(groupId, gatheringId)

        // LiveData 관찰
        observeViewModel()
        initializeMap()
    }

    private fun initializeMap() {
        val mapLifeCycleCallback = object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
                kakaoMap = null
            }

            override fun onMapError(error: Exception) {
                Log.e("KakaoMap", "맵 로드 에러", error)
                showToast("지도 로드 중 오류가 발생했습니다")
            }
        }

        val mapReadyCallback = object : KakaoMapReadyCallback() {
            override fun onMapReady(map: KakaoMap) {
                Log.d("KakaoMap", "맵이 준비되었습니다")
                kakaoMap = map
                // 맵이 준비되었을 때 pendingPlace가 있다면 표시
                pendingPlace?.let { place ->
                    updateMapMarker(place)
                }
            }
        }

        binding.mapView.start(mapLifeCycleCallback, mapReadyCallback)
    }

    private fun updateMapMarker(place: PlaceLocationResponse) {
        kakaoMap?.let { map ->
            try {
                val position = LatLng.from(
                    place.latitude.toDouble(),
                    place.longitude.toDouble()
                )

                // 카메라 이동
                map.moveCamera(CameraUpdateFactory.newCenterPosition(position, 15))

                val layer = map.labelManager?.layer
                layer?.removeAll()

                // 비트맵 리사이징
                val originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_map_marker_png)
                val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 48, 48, true)

                // 아이콘 스타일 생성 (리사이즈된 비트맵 사용)
                val markerStyle = LabelStyle.from(resizedBitmap)
                    .setZoomLevel(1)
                    .setAnchorPoint(0.5f, 0.5f)

                // 아이콘 라벨 추가
                layer?.addLabel(
                    LabelOptions.from(position).apply {
                        setStyles(map.labelManager?.addLabelStyles(
                            LabelStyles.from("markerStyle", markerStyle)
                        ))
                    }
                )

                binding.lLayoutGatheringInfoNoPlace.visibility = View.GONE
                binding.mapContainer.visibility = View.VISIBLE

            } catch (e: Exception) {
                Log.e("KakaoMap", "마커 업데이트 중 에러: ${e.message}", e)
                e.printStackTrace()
                showToast("위치 표시 중 오류가 발생했습니다")
            }
        } ?: run {
            Log.e("KakaoMap", "Map is not initialized")
        }
    }

    override fun initAfterBinding() {
        // 클릭 이벤트
        onClickListener()
    }

    // LiveData 관찰
    private fun observeViewModel() {
        // 모임 이름 정보 관찰
        viewModel.gatheringName.observe(viewLifecycleOwner) { name ->
            // 툴바 설정
            binding.toolbarGatheringInfo.tvToolbarTitle.text = name
        }

        // 날짜 정보 관찰
        viewModel.gatheringDate.observe(viewLifecycleOwner) { newDate ->
            date = newDate
            updateTextView()
        }

        // 시작 시간 정보 관찰
        viewModel.gatheringStartTime.observe(viewLifecycleOwner) { newStartTime ->
            startTime = newStartTime
            updateTextView()
        }

        // 종료 시간 정보 관찰
        viewModel.gatheringEndTime.observe(viewLifecycleOwner) { newEndTime ->
            endTime = newEndTime
            updateTextView()
        }

        // 중간 지점 정보 관찰
        viewModel.placeName.observe(viewLifecycleOwner) { place ->
            binding.tvGatheringInfoPlace.text = if (place.isNullOrEmpty()) {
                "중간 지점 입력하기"
            } else {
                "${place}역"
            }
        }

        // 소요 시간 관찰
        viewModel.subwayTime.observe(viewLifecycleOwner) { time ->
            if (time.isNullOrEmpty()) {
                // 시간 정보가 없을 때, "소요시간" 전체 표시
                binding.tvGatheringInfoSubwayTime.text = "소요시간"
                binding.tvGatheringInfoSubway.visibility = View.GONE
            } else {
                // 시간 정보가 있을 때, "n분 소요" 형태 표시
                binding.tvGatheringInfoSubwayTime.text = time
                binding.tvGatheringInfoSubway.visibility = View.VISIBLE
            }
        }


        // 좌표 정보 관찰
        // TODO: 사용자의 출발 좌표도 함께 지도에 마커 추가하기
        viewModel.gatheringPlace.observe(viewLifecycleOwner) { place ->
            // 좌표가 없다면
            if (place == null) {
                // default 뷰 띄우기
                binding.lLayoutGatheringInfoNoPlace.visibility = View.VISIBLE
                binding.mapContainer.visibility = View.GONE
            }
            // 좌표가 있다면
            else {
                binding.lLayoutGatheringInfoNoPlace.visibility = View.GONE
                binding.mapContainer.visibility = View.VISIBLE
                pendingPlace = place
                kakaoMap?.let {
                    updateMapMarker(place)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.resume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.pause()
    }

    // 모임 일정 업데이트
    @SuppressLint("SetTextI18n")
    private fun updateTextView() {
        try {
            // date가 비어있지 않은지 확인
            if (date.isNotEmpty() && startTime.isNotEmpty()) {
                // 날짜 포맷팅 (2024-12-03 -> 12월 3일)
                val dateParts = date.split("-")
                if (dateParts.size >= 3) {  // 배열 크기 체크
                    val month = dateParts[1].toIntOrNull() ?: return  // null 체크
                    val day = dateParts[2].toIntOrNull() ?: return    // null 체크

                    // 요일 구하기
                    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val dateObj = dateFormatter.parse(date) ?: return
                    val calendar = Calendar.getInstance()
                    calendar.time = dateObj
                    val dayOfWeek = when(calendar.get(Calendar.DAY_OF_WEEK)) {
                        Calendar.SUNDAY -> "일"
                        Calendar.MONDAY -> "월"
                        Calendar.TUESDAY -> "화"
                        Calendar.WEDNESDAY -> "수"
                        Calendar.THURSDAY -> "목"
                        Calendar.FRIDAY -> "금"
                        Calendar.SATURDAY -> "토"
                        else -> ""
                    }

                    // 시간 표시
                    val timeText = if (endTime.isNotEmpty()) {
                        "$startTime-$endTime"
                    } else {
                        startTime
                    }

                    // TextView에 날짜, 요일, 시간 설정
                    binding.tvGatheringInfoDate.text =
                        "${month}월 ${day}일 (${dayOfWeek}) $timeText"
                }
            }
        } catch (e: Exception) {
            // 에러 발생 시 기본 텍스트 표시
            binding.tvGatheringInfoDate.text = "$date $startTime"
            Log.e("GatheringInfo", "Error updating text view", e)
        }
    }

    // 클릭 이벤트
    private fun onClickListener() {
        // 뒤로가기 버튼 클릭 이벤트
        binding.toolbarGatheringInfo.ivToolbarBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // 모임 정보 수정하기 버튼 클릭 이벤트
        binding.lLayoutGatheringInfoDate.setOnClickListener {
            // TODO: 모임 정보 수정 화면으로 이동
            // TODO: 수정은 하지 말까...? ㅎ..
        }

        // 중간 지점 입력 버튼 클릭 이벤트
        binding.lLayoutGatheringInfoPlace.setOnClickListener {
            // 중간지점 화면으로 이동
            val actionToGatheringPlace = GatheringInfoFragmentDirections.actionGatheringInfoFrmToGatheringPlaceFrm(
                gatheringId = gatheringId,
                groupId = groupId,
                addressInfo = null
            )
            findNavController().navigate(actionToGatheringPlace)
        }
    }
}