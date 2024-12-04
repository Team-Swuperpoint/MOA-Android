package com.swuperpoint.moa_android.view.main.group

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.TimePicker
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.swuperpoint.moa_android.R
import com.swuperpoint.moa_android.databinding.FragmentCreateGatheringBinding
import com.swuperpoint.moa_android.view.base.BaseFragment
import com.swuperpoint.moa_android.view.main.group.calendar.SelectedDecorator
import com.swuperpoint.moa_android.widget.utils.CalendarUtils.Companion.getTodayDate
import com.swuperpoint.moa_android.widget.utils.CalendarUtils.Companion.getTodayDateList
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar

/* 모임 만들기 화면 */
class CreateGatheringFragment : BaseFragment<FragmentCreateGatheringBinding>(FragmentCreateGatheringBinding::inflate) {
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private var isEnable = false // 만들기 버튼 활성화 여부
    private lateinit var selectDate: LocalDate // 현재 사용자가 선택 중인 날짜
    private var groupId: String = "" // 그룹id(pk), String 타입으로 변경


    @SuppressLint("SetTextI18n")
    override fun initViewCreated() {
        val args: MemberFragmentArgs by navArgs()
        groupId = args.groupId

        // 앱 바 타이틀 설정
        setToolbarTitle(binding.toolbarCreateGathering.tvToolbarTitle, "모임 만들기")

        // 상태바 색상 변경
        changeStatusbarColor(R.color.white, true)

        // 월간 캘린더
        binding.mcvCreateGatheringCalendarview.topbarVisible = false // 년도, 좌우 버튼 숨기기

        // 기본 날짜를 오늘로 설정
        selectDate = LocalDate.parse(getTodayDate().toString().split("T")[0])

        // 기본 날짜 캘린더에 적용
        val dateFormatList = selectDate.toString().split("-") // 정보 분리
        binding.mcvCreateGatheringCalendarview.selectedDate = CalendarDay.from(dateFormatList[0].toInt(), dateFormatList[1].toInt(), dateFormatList[2].toInt())
        val decorator = SelectedDecorator(requireContext(), binding.mcvCreateGatheringCalendarview.selectedDate)
        binding.mcvCreateGatheringCalendarview.addDecorator(decorator) // 월간 캘린더에 dot 적용
        binding.mcvCreateGatheringCalendarview.currentDate = binding.mcvCreateGatheringCalendarview.selectedDate // 월간 캘린더에 선택 날짜 적용

        // 이번달 날짜 설정
        val todayList = getTodayDateList()[0].toString().split("-")
        binding.tvCreateGatheringYearMonth.text = "${todayList[0]}년 ${todayList[1]}월"
    }

    override fun initAfterBinding() {
        // 클릭 이벤트
        onClickListener()

        // 모임 이름을 입력했다면 버튼 활성화 여부 확인
        binding.edtCreateGatheringTitle.doOnTextChanged { text, start, before, count ->
            if (text.toString().isNotEmpty()) {
                binding.edtCreateGatheringTitle.setBackgroundResource(R.drawable.ic_text_field_selected_358)
            }
            else {
                binding.edtCreateGatheringTitle.setBackgroundResource(R.drawable.ic_text_field_unselected_358)
            }

            // 버튼 UI 변경
            updateButtonUI()
        }
    }

    // 버튼 클릭 이벤트
    @SuppressLint("SetTextI18n")
    private fun onClickListener() {
        // 뒤로가기 버튼 클릭 이벤트
        binding.toolbarCreateGathering.ivToolbarBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // 화면 바깥 클릭 시
        binding.lLayoutCreateGathering.setOnClickListener {
            getHideKeyboard(binding.root) // 키보드 닫기
        }

        // 모임 만들기 버튼 클릭 이벤트
        binding.btnCreateGatheringCreate.setOnClickListener {
            // 만들기 가능한 상태라면
            if (isEnable) {
                val gathering = hashMapOf(
                    "gatheringId" to "",
                    "gatheringName" to binding.edtCreateGatheringTitle.text.toString(),
                    "date" to selectDate.toString(),
                    "gatheringStartTime" to binding.tvCreateGatheringStartTime.text.toString(), // 만남 시작 시간 저장
                    "gatheringEndTime" to binding.tvCreateGatheringEndTime.text.toString(),     // 종료 시간 추가
                    // TODO: location 필드 추가
                    "gatheringImgURL" to "",
                    "createdAt" to com.google.firebase.Timestamp.now(),
                    "createdBy" to auth.currentUser?.uid,  // 생성자 UID 추가
                    "participants" to listOf(auth.currentUser?.uid)  // 참여자 목록에 생성자 UID 추가
                )

                db.collection("groups")
                    .document(groupId)
                    .collection("gatherings")
                    .add(gathering)
                    .addOnSuccessListener { documentReference ->
                        documentReference.update("gatheringId", documentReference.id)
                            .addOnSuccessListener {
                                // 그룹의 최근 모임 정보 업데이트
                                db.collection("groups")
                                    .document(groupId)
                                    .update("recentGathering", selectDate.toString())
                                    .addOnSuccessListener {
                                        showToast("새로운 모임을 만들었습니다")
                                        findNavController().popBackStack()
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e(
                                            "CreateGathering",
                                            "Error updating recentGathering",
                                            e
                                        )
                                        showToast("모임 생성 중 오류가 발생했습니다")
                                    }
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e("CreateGathering", "Error creating gathering", e)
                        showToast("모임 생성 중 오류가 발생했습니다")
                    }
            }
        }

        // 월간 캘린더 날짜 클릭 이벤트
        binding.mcvCreateGatheringCalendarview.setOnDateChangedListener { widget, date, selected ->
            val selectedDate = binding.mcvCreateGatheringCalendarview.selectedDate!!
            val eventDecorator = SelectedDecorator(requireContext(), selectedDate)
            binding.mcvCreateGatheringCalendarview.addDecorator(eventDecorator)

            // 날짜 선택 정보 업데이트
            selectDate = LocalDate.of(selectedDate.year, selectedDate.month, selectedDate.day)
        }

        // 이전 달 조회 버튼 클릭 이벤트
        binding.ivCreateGatheringPrevMonth.setOnClickListener {
            binding.mcvCreateGatheringCalendarview.goToPrevious()
        }

        // 다음달 조회 버튼 클릭 이벤트
        binding.ivCreateGatheringNextMonth.setOnClickListener {
            binding.mcvCreateGatheringCalendarview.goToNext()
        }

        // 캘린더의 달이 바뀔 때마다 dot 와 달 정보 바꿔주기
        binding.mcvCreateGatheringCalendarview.setOnMonthChangedListener { widget, date ->
            // 기존에 설정되어 있던 decorators 초기화
            /*binding.mcvHomeMonthlyCalendarview.removeDecorators()
            binding.mcvHomeMonthlyCalendarview.invalidateDecorators()*/

            // 뷰에 년도와 월 정보 업데이트
            binding.tvCreateGatheringYearMonth.text = "${date.year}년 ${date.month}월"
        }

        // 모임 시작 시간 클릭 이벤트
        binding.tvCreateGatheringStartTime.setOnClickListener {
            val startTime = binding.tvCreateGatheringStartTime
            val startTimepicker = binding.timepickerCreateGatheringStartTime
            val endTimepicker = binding.timepickerCreateGatheringEndTime

            // 타임피커 닫기
            if (startTimepicker.visibility == View.VISIBLE) {
                startTimepicker.visibility = View.GONE
            }
            // 타임 피커 띄우기
            else {
                endTimepicker.visibility = View.GONE
                startTimepicker.visibility = View.VISIBLE
            }

            getTimePicker(startTime, startTimepicker)
        }

        // 모임 종료 시간 클릭 이벤트
        binding.tvCreateGatheringEndTime.setOnClickListener {
            val endTime = binding.tvCreateGatheringEndTime
            val endTimepicker = binding.timepickerCreateGatheringEndTime
            val startTimepicker = binding.timepickerCreateGatheringStartTime

            // 타임피커 닫기
            if (endTimepicker.visibility == View.VISIBLE) {
                endTimepicker.visibility = View.GONE
            }
            // 타임 피커 띄우기
            else {
                startTimepicker.visibility = View.GONE
                endTimepicker.visibility = View.VISIBLE
            }

            getTimePicker(endTime, endTimepicker)
        }
    }

    // 뷰 형식으로 띄우는 타임피커
    @SuppressLint("SimpleDateFormat")
    fun getTimePicker(time: TextView, timepicker: TimePicker) {
        // 초기값 설정
        if (time.text.toString().isNotBlank()) {
            // 시간 나누기
            val tempArray = time.text.toString().split(":")

            timepicker.hour = tempArray[0].toInt()
            timepicker.minute = tempArray[1].toInt()
        }

        val calendar = Calendar.getInstance()

        timepicker.setOnTimeChangedListener { timePicker, hour, minute ->
            val tempHour = timePicker.hour
            val tempMinute = timePicker.minute

            calendar.set(Calendar.HOUR_OF_DAY, tempHour)
            calendar.set(Calendar.MINUTE, tempMinute)

            val sdf = SimpleDateFormat("HH:mm").format(calendar.time)
            time.text = sdf
        }
    }

    // 버튼 UI 변경
    private fun updateButtonUI() {
        // 추가 가능
        if (binding.edtCreateGatheringTitle.text.toString().isNotEmpty()) {
            binding.btnCreateGatheringCreate.setBackgroundResource(R.drawable.ic_button_wide_selected_358)
            binding.btnCreateGatheringCreate.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            isEnable = true
        }
        // 추가 불가능
        else {
            binding.btnCreateGatheringCreate.setBackgroundResource(R.drawable.ic_button_wide_unselected_358)
            binding.btnCreateGatheringCreate.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_400))
            isEnable = false
        }
    }
}