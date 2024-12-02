package com.swuperpoint.moa_android.view.main.group

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.swuperpoint.moa_android.R
import com.swuperpoint.moa_android.data.remote.model.group.kakaomap.KakaoMapClass
import com.swuperpoint.moa_android.data.remote.model.group.kakaomap.SearchAddressResponse
import com.swuperpoint.moa_android.databinding.FragmentAddressBinding
import com.swuperpoint.moa_android.view.base.BaseFragment
import com.swuperpoint.moa_android.view.main.group.adapter.AddressRVAdapter
import com.swuperpoint.moa_android.view.main.group.data.AddressItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/* 주소 입력 화면 */
class AddressFragment : BaseFragment<FragmentAddressBinding>(FragmentAddressBinding::inflate) {
    private var addressList = ArrayList<AddressItem>() // 검색 결과 리스트
    private var addressAdapter = AddressRVAdapter(addressList) // 검색 결과 어댑터
    private var keyword: String? = null // 사용자가 입력한 검색 키워드
    private var gatheringId: String = "" // 모임 id, Long -> String 변경
    private var groupId: String = ""  // groupId 추가

    @SuppressLint("SetTextI18n")
    override fun initViewCreated() {
        // 다른 화면에서 전달받은 모임id 설정
        val args: AddressFragmentArgs by navArgs()
        gatheringId = args.gatheringId
        groupId = args.groupId

        // 툴바 설정
        binding.toolbarAddress.tvToolbarTitle.text = "출발 장소 검색"
    }

    override fun initAfterBinding() {
        // 실시간으로 검색 목록 바꾸기
        showAddressList()

        // 어댑터 연결
        binding.rvAddress.adapter = addressAdapter

        // 출발 장소 클릭 이벤트
        addressAdapter.onClickListener = { item ->
            Log.d("출발 장소 선택!", item.toString())
            val actionToGatheringPlace = AddressFragmentDirections.actionAddressFrmToGatheringPlaceFrm(
                gatheringId = gatheringId,
                groupId = groupId,
                addressInfo = item
            )
            findNavController().navigate(actionToGatheringPlace)
        }

        // 뒤로가기 버튼 클릭 이벤트
        binding.toolbarAddress.ivToolbarBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    // 사용자가 입력한 값에 따라 검색 목록 변경
    private fun showAddressList() {
        binding.edtAddress.addTextChangedListener(object : TextWatcher {
            // 검색 전
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.rvAddress.visibility = View.VISIBLE // 리사이클러뷰 보이기
            }

            // 검색 중
            override fun onTextChanged(charSequence: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // 값이 있다면
                if (charSequence != null) {
                    if (charSequence.isNotBlank()) {
                        keyword = charSequence.toString()
                        searchAddress(keyword!!) // 검색 목록 찾기
                    }
                }
                // 값이 없다면
                else {
                    binding.rvAddress.visibility = View.GONE // 리사이클러뷰 숨기기
                    binding.edtAddress.setBackgroundResource(R.drawable.ic_text_field_unselected_358) // 텍스트 박스 바꾸기
                }
            }

            // 검색 후
            @SuppressLint("ClickableViewAccessibility")
            override fun afterTextChanged(p0: Editable?) {
                binding.edtAddress.setBackgroundResource(R.drawable.ic_text_field_selected_358) // 텍스트 박스 바꾸기

                keyword = binding.edtAddress.text.toString()
                searchAddress(keyword!!) // 검색 목록 찾기
            }
        })
    }

    // 사용자가 입력한 키워드에 따른 검색 함수
    private fun searchAddress(keyword: String) {
        val call = KakaoMapClass.api.getSearchResult(KakaoMapClass.API_KEY, keyword) // 검색 조건 입력

        // Kakao Map API 서버에 요청
        call.enqueue(object : Callback<SearchAddressResponse> {
            override fun onResponse(
                call: Call<SearchAddressResponse>,
                response: Response<SearchAddressResponse>
            ) {
                // 요청 성공
                getKeywordAddress(response.body())
            }

            override fun onFailure(call: Call<SearchAddressResponse>, t: Throwable) {
                // 요청 실패
                Log.e("AddressFragment : ", "서버 요청 실패: ${t.message}")
            }
        })
    }

    // 검색 결과를 처리하는 함수
    @SuppressLint("NotifyDataSetChanged")
    private fun getKeywordAddress(searchResponse: SearchAddressResponse?) {
        if (searchResponse?.documents != null) {
            // 검색 결과 있음
            addressList.clear() // 주소 목록 초기화

            // 검색 결과 목록을 리사이클러뷰에 추가
            for (document in searchResponse.documents) {
                val item = AddressItem(
                    document.place_name, // 장소명
                    document.address_name, // 지번 주소
                    document.road_address_name, // 도로명 주소
                    document.x, // 경도
                    document.y // 위도
                )
                addressList.add(item)
            }

            addressAdapter.notifyDataSetChanged() // 어댑터 갱신
        }
        else {
            // 검색 결과 없음
            Log.e("AddressFragment : ", "검색 결과 없음")
        }
    }
}