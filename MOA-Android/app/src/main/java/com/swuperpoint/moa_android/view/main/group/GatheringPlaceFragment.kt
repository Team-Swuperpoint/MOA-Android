package com.swuperpoint.moa_android.view.main.group

import android.annotation.SuppressLint
import android.util.Log
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
    private val viewModel: GatheringPlaceViewModel by viewModels() // 뷰모델
    private var memberAdapter = GroupMemberRVAdapter() // 그룹원 리스트 어댑터
    private val placeAdapter = RecommendPlaceRVAdapter() // 추천 중간지점 리스트 어댑터
    private var gatheringId: Long = 0 // 모임 id
    private var addressItem: AddressItem? = null // 사용자가 선택한 주소 정보

    override fun initViewCreated() {
        // 다른 화면에서 전달받은 모임id 설정
        val args: GatheringPlaceFragmentArgs by navArgs()
        gatheringId = args.gatheringId
        addressItem = args.addressInfo

        // 새롭게 선택한 주소 정보가 있다면 값 업데이트
        viewModel.updateAddressItem(addressItem)

        // 데이터 로드
        viewModel.fetchGatheringPlace(gatheringId)

        // LiveData 관찰
        observeViewModel()
    }

    override fun initAfterBinding() {
        // 클릭 이벤트
        onClickListener()

        // 그룹원 리스트 어댑터 연결
        binding.rvGatheringPlaceMember.adapter = memberAdapter

        // 추천 중간 지점 리스트 어댑터 연결
        binding.rvGatheringPlaceRecommends.adapter = placeAdapter
    }

    // LiveData 관찰
    @SuppressLint("SetTextI18n")
    private fun observeViewModel() {
        // 중간지점을 입력한 그룹원 리스트 정보 관찰
        viewModel.memberList.observe(viewLifecycleOwner) { members ->
            memberAdapter.updateMembers(members)
            binding.tvGatheringPlaceParticipant.text = memberAdapter.itemCount.toString()
        }

        // 총 그룹원 인원 수
        viewModel.memberNum.observe(viewLifecycleOwner) { num ->
            binding.tvGatheringPlaceMemberNum.text = num.toString()
        }

        // 사용자가 입력한 출발 장소 이름
        viewModel.userPlaceName.observe(viewLifecycleOwner) { name ->
            val field = binding.tvGatheringPlaceStartPlace
            val layout = binding.fLayoutGatheringPlaceStartPlace

            // 사전에 입력했던 장소가 있다면
            if (name != null) {
                field.text = name
                field.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_900))
                layout.setBackgroundResource(R.drawable.ic_text_field_selected_358)
            }
            // 없다면
            else {
                field.text = "출발 장소"
                field.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_500))
                layout.setBackgroundResource(R.drawable.ic_text_field_unselected_358)
            }
        }

        // 추천 중간 지점 리스트 관찰
        viewModel.placeList.observe(viewLifecycleOwner) { places ->
            if (places != null) {
                placeAdapter.updatePlaces(places)
            }
        }
    }

    // 클릭 이벤트
    private fun onClickListener() {
        // 뒤로가기 버튼 클릭 이벤트
        binding.toolbarGatheringPlace.ivToolbarBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // 출발 장소화면으로 이동 버튼 클릭 이벤트
        binding.fLayoutGatheringPlaceStartPlace.setOnClickListener {
            val actionToAddress = GatheringPlaceFragmentDirections.actionGatheringPlaceFrmToAddressFrm(gatheringId, null)
            findNavController().navigate(actionToAddress)
        }

        // 중간지점 찾기 버튼 클릭 이벤트
        binding.btnGatheringPlaceFind.setOnClickListener {
            // TODO: 중간지점 찾기 API 연결

            // TODO: 받아온 정보로 추천 중간 지점 리스트 어댑터 업데이트
        }

        // 핫플레이스 찾기 버튼 클릭 이벤트
        binding.btnGatheringPlaceHotFind.setOnClickListener {
            // TODO: 핫플레이스 찾기 API 연결

            // TODO: 받아온 정보로 추천 중간 지점 리스트 어댑터 업데이트
        }

        // 추천 중간 지점 선택 이벤트
        placeAdapter.onClickListener = { position ->
            showToast("$position 중간 지점 선택!")

            // TODO: 선택한 중간지점 정보를 서버에 전송

            // TODO: 데이터 전송에 성공했다면 중간 지점을 새롭게 선택했다면 모임 화면으로 이동
            val actionToGatheringInfo = GatheringPlaceFragmentDirections.actionGatheringPlaceFrmToGatheringInfoFrm(gatheringId)
            findNavController().navigate(actionToGatheringInfo)
        }
    }
}