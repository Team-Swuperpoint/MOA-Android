package com.swuperpoint.moa_android.view.main.group

import android.annotation.SuppressLint
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.swuperpoint.moa_android.R
import com.swuperpoint.moa_android.databinding.FragmentGatheringPlaceBinding
import com.swuperpoint.moa_android.view.base.BaseFragment
import com.swuperpoint.moa_android.view.main.group.adapter.GroupMemberRVAdapter
import com.swuperpoint.moa_android.view.main.group.adapter.RecommendPlaceRVAdapter
import com.swuperpoint.moa_android.view.main.group.data.AddressItem
import com.swuperpoint.moa_android.viewmodel.main.group.GatheringPlaceViewModel

/* 중간 지점 찾기 화면 */
class GatheringPlaceFragment : BaseFragment<FragmentGatheringPlaceBinding>(FragmentGatheringPlaceBinding::inflate) {
    private val viewModel: GatheringPlaceViewModel by viewModels()
    private var memberAdapter = GroupMemberRVAdapter()
    private val placeAdapter = RecommendPlaceRVAdapter()
    private var gatheringId: String = ""
    private var groupId: String = ""  // 추가
    private var addressItem: AddressItem? = null

    override fun initViewCreated() {
        val args: GatheringPlaceFragmentArgs by navArgs()
        gatheringId = args.gatheringId
        addressItem = args.addressInfo
        groupId = args.groupId

        // midpointCalculator 초기화를 위해 context 전달
        viewModel.initContext(requireContext())

        // 새롭게 선택한 주소 정보가 있다면 값 업데이트
        viewModel.updateAddressItem(addressItem)

        // Firebase에서 데이터 로드
        viewModel.fetchGatheringPlace(gatheringId)

        observeViewModel()
    }

    override fun initAfterBinding() {
        onClickListener()
        binding.rvGatheringPlaceMember.adapter = memberAdapter
        binding.rvGatheringPlaceRecommends.adapter = placeAdapter
    }

    @SuppressLint("SetTextI18n")
    private fun observeViewModel() {
        // 로딩 상태 관찰
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // TODO: 로딩 UI 처리
        }

        // 에러 상태 관찰
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                showToast(it)
            }
        }

        // 그룹원 리스트 정보 관찰
        viewModel.memberList.observe(viewLifecycleOwner) { members ->
            memberAdapter.updateMembers(members)
            binding.tvGatheringPlaceParticipant.text = memberAdapter.itemCount.toString()
        }

        viewModel.memberNum.observe(viewLifecycleOwner) { num ->
            binding.tvGatheringPlaceMemberNum.text = num.toString()
        }

        viewModel.userPlaceName.observe(viewLifecycleOwner) { name ->
            val field = binding.tvGatheringPlaceStartPlace
            val layout = binding.fLayoutGatheringPlaceStartPlace

            if (name != null) {
                field.text = name
                field.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_900))
                layout.setBackgroundResource(R.drawable.ic_text_field_selected_358)
            } else {
                field.text = "출발 장소"
                field.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_500))
                layout.setBackgroundResource(R.drawable.ic_text_field_unselected_358)
            }
        }

        viewModel.placeList.observe(viewLifecycleOwner) { places ->
            if (places != null) {
                placeAdapter.updatePlaces(places)
            }
        }
    }

    private fun onClickListener() {
        binding.toolbarGatheringPlace.ivToolbarBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.fLayoutGatheringPlaceStartPlace.setOnClickListener {
            val actionToAddress = GatheringPlaceFragmentDirections.actionGatheringPlaceFrmToAddressFrm(
                gatheringId = gatheringId,
                groupId = groupId,
                addressInfo = null
            )
            findNavController().navigate(actionToAddress)
        }

        // 테스트 데이터 추가를 위한 롱클릭 리스너
        binding.btnGatheringPlaceFind.setOnLongClickListener {
            viewModel.addTestMemberPlaces(groupId, gatheringId)
            true
        }

        // 중간지점 찾기 버튼
        binding.btnGatheringPlaceFind.setOnClickListener {
            if (addressItem == null) {
                showToast("출발 장소를 먼저 입력해주세요")
                return@setOnClickListener
            }

            // Firebase를 통해 중간 지점 계산
            viewModel.fetchGatheringPlace(gatheringId)
        }

        // 핫플레이스 찾기 버튼
        binding.btnGatheringPlaceHotFind.setOnClickListener {
            // 중간 지점이 계산되어 있지 않다면
            if (viewModel.placeList.value == null) {
                showToast("중간 지점을 먼저 계산해주세요")
                return@setOnClickListener
            }

            // TODO: 핫플레이스 찾기 기능 구현
            showToast("준비 중인 기능입니다")
        }

        // 추천 중간 지점 선택
        placeAdapter.onClickListener = { position ->
            viewModel.placeList.value?.get(position)?.let { selectedPlace ->
                val gatheringPlace = hashMapOf(
                    "placeName" to selectedPlace.placeName,
                    "address" to selectedPlace.address,
                    "subwayTime" to selectedPlace.subwayTime,
                    "latitude" to selectedPlace.latitude,
                    "longitude" to selectedPlace.longitude
                )

                // Firebase에 선택한 중간 지점 저장
                viewModel.saveSelectedPlace(gatheringId, gatheringPlace) { success ->
                    if (success) {
                        val actionToGatheringInfo = GatheringPlaceFragmentDirections
                            .actionGatheringPlaceFrmToGatheringInfoFrm(gatheringId, groupId)
                        findNavController().navigate(actionToGatheringInfo)
                    } else {
                        showToast("중간 지점 저장에 실패했습니다")
                    }
                }
            }
        }
    }
}