package com.qst.crop.service;

import com.qst.crop.entity.Address;

import java.util.List;

public interface AddressService {

    /** 查询某用户的全部地址（默认地址置顶） */
    List<Address> listByUser(String userName);

    /** 新增地址（若设为默认，自动取消该用户其它默认地址） */
    Address add(Address address);

    /** 更新地址（需校验归属，并处理默认地址互斥） */
    boolean update(Address address);

    /** 删除地址（需校验归属） */
    boolean delete(Integer id, String userName);
}
