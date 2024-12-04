package com.swuperpoint.moa_android.view.main.timeline

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.swuperpoint.moa_android.R
import com.swuperpoint.moa_android.data.remote.model.group.CreateTimelineGatheringResponse
import com.swuperpoint.moa_android.data.remote.model.group.CreateTimelineGroupResponse
import com.swuperpoint.moa_android.databinding.FragmentCreateTimelineBinding
import com.swuperpoint.moa_android.view.base.BaseFragment
import com.swuperpoint.moa_android.view.main.timeline.adapter.CreateTimelinePhotoRVAdapter
import com.swuperpoint.moa_android.viewmodel.main.timeline.CreateTimelineViewModel

/* 타임라인 만들기 화면 */
class CreateTimelineFragment : BaseFragment<FragmentCreateTimelineBinding>(FragmentCreateTimelineBinding::inflate) {
    private val viewModel: CreateTimelineViewModel by viewModels() // 뷰모델
    private var isEnable = false // 추가하기 버튼 활성화 여부

    private lateinit var groupSpinnerAdapter: ArrayAdapter<CreateTimelineGroupResponse> // 그룹 리스트 어댑터
    private lateinit var gatheringSpinnerAdapter: ArrayAdapter<CreateTimelineGatheringResponse> // 모임 리스트 어댑터
    private var photoAdapter = CreateTimelinePhotoRVAdapter() // 사진 리스트 어댑터

    private var selectedGroupId: String = "" // 선택한 그룹 id
    private var selectedGatheringId: String = "" // 선택한 모임 id

    override fun initViewCreated() {
        // 앱 바 타이틀 설정
        setToolbarTitle(binding.toolbarCreateTimeline.tvToolbarTitle, "타임라인 만들기")

        // 상태바 색상 변경
        changeStatusbarColor(R.color.white, true)

        // 바텀 네비게이션 숨기기
        mainActivity?.hideLBNV(true)

        // LiveData 관찰
        observeViewModel()

        // 데이터 로드
        viewModel.fetchCreateTimeline()
    }

    override fun initAfterBinding() {
        // 사진 리스트 어댑터 연결
        binding.rvCreateTimelinePhotos.adapter = photoAdapter

        // 클릭 이벤트
        onClickListener()
    }

    // 버튼 클릭 이벤트
    private fun onClickListener() {
        // 뒤로가기 버튼 클릭 이벤트
        binding.toolbarCreateTimeline.ivToolbarBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // 그룹 클릭 이벤트
        binding.spinnerCreateTimelineGroup.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                for (i in 0 until groupSpinnerAdapter.count) {
                    if (i == position) {
                        selectedGroupId = groupSpinnerAdapter.getItem(position)?.groupId.toString()
                    }
                }

                // 버튼 색상 전환 여부 확인
                updateButtonUI()
            }

            // 아무것도 선택 안 했을 경우
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        // 모임 클릭 이벤트
        binding.spinnerCreateTimelineGathering.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                for (i in 0 until gatheringSpinnerAdapter.count) {
                    if (i == position) {
                        selectedGatheringId = gatheringSpinnerAdapter.getItem(position)?.gatheringId.toString()
                    }
                }

                // 버튼 색상 전환 여부 확인
                updateButtonUI()
            }

            // 아무것도 선택 안 했을 경우
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        // 사진 추가 버튼 클릭 이벤트
        binding.btnCreateTimelineGallery.setOnClickListener {
            // 권한 체크
            checkGalleryPermission()
        }

        // 사진 삭제 버튼 클릭 이벤트
        photoAdapter.onClickListener = { pos ->
            photoAdapter.deletePhoto(position = pos)
        }

        // 타임라인 만들기 버튼 클릭 이벤트
        binding.btnCreateTimelineCreate.setOnClickListener {
            Log.d("타임라인 데이터", "selectedGroupId: $selectedGroupId, selectedGatheringId: $selectedGatheringId, photos: ${photoAdapter.getPhotos()}")
            if (isEnable) {
                // TODO: 타임라인 만들기 API 호출
                // TODO: 타임라인 만들기에 성공했다면, 응답 값으로 타임라인id 반환 및 타임라인 정보 화면으로 이동
                val actionToTimelineInfo = CreateTimelineFragmentDirections.actionCreateTimelineFrmToTimelineInfoFrm(timelineId = "0")
                findNavController().navigate(actionToTimelineInfo)
            }
        }
    }

    // 버튼 UI 변경
    private fun updateButtonUI() {
        // 추가 가능
        if (selectedGroupId.isNotBlank() && selectedGatheringId.isNotBlank() && photoAdapter.itemCount != 0) {
            binding.btnCreateTimelineCreate.setBackgroundResource(R.drawable.ic_button_wide_selected_358)
            binding.btnCreateTimelineCreate.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            isEnable = true
        }
        // 추가 불가능
        else {
            binding.btnCreateTimelineCreate.setBackgroundResource(R.drawable.ic_button_wide_unselected_358)
            binding.btnCreateTimelineCreate.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_400))
            isEnable = false
        }
    }

    // LiveData 관찰
    private fun observeViewModel() {
        // 그룹, 모임 리스트 정보 관찰
        viewModel.groupList.observe(viewLifecycleOwner) { groups ->
            if (groups != null) {
                // 스피너 어댑터 초기화 코드
                groupSpinnerAdapter = object : ArrayAdapter<CreateTimelineGroupResponse>(
                    requireContext(),
                    R.layout.spinner_dropdown_selected,
                    groups // 데이터 리스트
                ) {
                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val view = super.getView(position, convertView, parent)
                        val textView = view.findViewById<TextView>(R.id.spinner_checked_item_tv)
                        textView.text = getItem(position)?.groupName // groupName만 표시
                        return view
                    }

                    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val view = super.getDropDownView(position, convertView, parent)
                        val textView = view.findViewById<TextView>(R.id.spinner_checked_item_tv)
                        textView.text = getItem(position)?.groupName // groupName만 표시
                        return view
                    }
                }
                binding.spinnerCreateTimelineGroup.adapter = groupSpinnerAdapter
            }
        }

        // 모임 리스트 정보 관찰
        viewModel.gatheringList.observe(viewLifecycleOwner) { gatherings ->
            if (gatherings != null) {
                // 스피너 어댑터 초기화 코드
                gatheringSpinnerAdapter = object : ArrayAdapter<CreateTimelineGatheringResponse>(
                    requireContext(),
                    R.layout.spinner_dropdown_selected,
                    gatherings // 데이터 리스트
                ) {
                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val view = super.getView(position, convertView, parent)
                        val textView = view.findViewById<TextView>(R.id.spinner_checked_item_tv)
                        textView.text = getItem(position)?.gatheringName // name만 표시
                        return view
                    }

                    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val view = super.getDropDownView(position, convertView, parent)
                        val textView = view.findViewById<TextView>(R.id.spinner_checked_item_tv)
                        textView.text = getItem(position)?.gatheringName // name만 표시
                        return view
                    }
                }
                binding.spinnerCreateTimelineGathering.adapter = gatheringSpinnerAdapter
            }
        }
    }

    // 갤러리 접근 권한 있는지 확인하는 함수
    private fun checkGalleryPermission() {
        // 갤러리 접근 권한이 있다면
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED) {
            goGallery() // 갤러리로 이동
        }
        // 권한이 없다면
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestGalleryPermissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
            }
            else {
                requestGalleryPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    // 갤러리 권한 여부 묻기
    private val requestGalleryPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        // 권한 허용
        if (isGranted) {
            goGallery() // 갤러리로 이동
        }
        // 권한 거부
        else {
            showMediaPermissionPopup() // 권한 설명 팝업 띄우기
        }
    }

    // 갤러리 접근 권한 확인 팝업 띄우기
    private fun showMediaPermissionPopup() {
        AlertDialog.Builder(context)
            .setTitle("권한을 허용해주세요")
            .setMessage("갤러리 접근 권한을 허용해주세요")
            .setPositiveButton("동의") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${requireContext().packageName}"))
                intent.addCategory(Intent.CATEGORY_DEFAULT)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
            .setNegativeButton("취소") { _, _ -> }
            .create()
            .show()
    }

    // 갤러리로 이동한 후의 로직 처리 함수
    private fun goGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.action = Intent.ACTION_GET_CONTENT
        startForResult.launch(intent) // 이미지 선택 후, 정보 가져오기
    }

    // 갤러리에서 이미지를 선택한 후 정보 가져오기
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                // 이미지 가져와서 적용하기
                Activity.RESULT_OK -> {
                    // 서버에 사진 전달
                    val fileUri = result.data?.clipData
                    if  (fileUri != null) {
                        for (i in 0 until fileUri.itemCount) {
                            val uri = result.data?.clipData!!.getItemAt(i).uri
                            photoAdapter.updatePhotos(uri)
                        }

                        // 버튼 UI 업데이트
                        updateButtonUI()
                    }
                }
                Activity.RESULT_CANCELED -> {
                    // 사진 선택 취소
                }
                else -> Toast.makeText(requireContext(), "사진을 가져오지 못했습니다", Toast.LENGTH_SHORT).show()
            }
        }
}