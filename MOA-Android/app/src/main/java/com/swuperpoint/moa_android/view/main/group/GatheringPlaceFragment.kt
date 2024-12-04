package com.swuperpoint.moa_android.view.main.group

import android.annotation.SuppressLint
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.swuperpoint.moa_android.R
import com.swuperpoint.moa_android.databinding.FragmentGatheringPlaceBinding
import com.swuperpoint.moa_android.util.Coordinate
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
    private val auth = Firebase.auth

    // AddressItem 선택 시 자동으로 저장되도록 수정
    override fun initViewCreated() {
        val args: GatheringPlaceFragmentArgs by navArgs()
        gatheringId = args.gatheringId
        addressItem = args.addressInfo
        groupId = args.groupId

        viewModel.initContext(requireContext())

        // 새로운 주소 선택 시 자동 저장
        addressItem?.let { address ->
            auth.currentUser?.uid?.let { userId ->
                viewModel.updateAddressItem(address, groupId, gatheringId, userId)
            }
        }

        viewModel.fetchGatheringPlace(groupId, gatheringId)
        viewModel.setGroupId(groupId)

        observeViewModel()
    }

    override fun initAfterBinding() {
        onClickListener()
        binding.rvGatheringPlaceMember.adapter = memberAdapter
        binding.rvGatheringPlaceRecommends.adapter = placeAdapter
    }

    @SuppressLint("SetTextI18n")
    private fun observeViewModel() {
        // 그룹원 프로필 리스트 업데이트
        viewModel.memberList.observe(viewLifecycleOwner) { members ->
            memberAdapter.updateMembers(members)
        }

        // 로딩 상태 관찰
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // TODO: 로딩 UI 처리
        }
        // 기존 observer 유지
        viewModel.isButtonEnabled.observe(viewLifecycleOwner) { isEnabled ->
            binding.btnGatheringPlaceFind.isEnabled = isEnabled
        }

        viewModel.progressCount.observe(viewLifecycleOwner) { count ->
            binding.tvGatheringPlaceParticipant.text = count.substringBefore("/")
            binding.tvGatheringPlaceMemberNum.text = count.substringAfter("/")
        }

        // 에러 상태 관찰
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                showToast(it)
            }
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
            places?.let {
                if (it.isNotEmpty()) {
                    placeAdapter.updatePlaces(it)
                    binding.rvGatheringPlaceRecommends.visibility = View.VISIBLE
                    binding.btnGatheringPlaceHotFind.isEnabled = true
                }
            }
        }

        viewModel.calculatingMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                binding.layoutLoading.visibility = View.VISIBLE
                binding.rvGatheringPlaceRecommends.visibility = View.GONE
            } else {
                binding.layoutLoading.visibility = View.GONE
                binding.rvGatheringPlaceRecommends.visibility = View.VISIBLE
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

        // 중간지점 찾기 버튼
        binding.btnGatheringPlaceFind.setOnClickListener {
            if (addressItem == null) {
                showToast("출발 장소를 먼저 입력해주세요")
                return@setOnClickListener
            }

            // 위치 저장
            viewModel.saveUserLocation(
                groupId,  // 추가
                gatheringId,
                auth.currentUser?.uid ?: return@setOnClickListener,
                addressItem ?: return@setOnClickListener
            )
        }

        // 핫플레이스 찾기 버튼
        binding.btnGatheringPlaceHotFind.setOnClickListener {
            if (viewModel.placeList.value == null) {
                showToast("중간 지점을 먼저 계산해주세요")
                return@setOnClickListener
            }

            val coordinates = viewModel.placeList.value?.map { place ->
                Coordinate(
                    lat = place.latitude.toString(),
                    lon = place.longitude.toString()
                )
            } ?: emptyList()

            viewModel.findHotplaceStation(coordinates)
        }

        // 추천 중간 지점 선택
        // placeAdapter 클릭 리스너 수정
        placeAdapter.onClickListener = { position ->
            viewModel.placeList.value?.get(position)?.let { selectedPlace ->
                viewModel.saveSelectedPlace(gatheringId, selectedPlace) { success ->
                    if (success) {
                        findNavController().navigate(
                            GatheringPlaceFragmentDirections
                                .actionGatheringPlaceFrmToGatheringInfoFrm(gatheringId, groupId)
                        )
                    } else {
                        showToast("중간 지점 저장에 실패했습니다")
                    }
                }
            }
        }
    }
}