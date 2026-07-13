package com.qst.crop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.qst.crop.dao.AddressMapper;
import com.qst.crop.entity.Address;
import com.qst.crop.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressMapper addressMapper;

    @Override
    public List<Address> listByUser(String userName) {
        QueryWrapper<Address> wrapper = new QueryWrapper<>();
        wrapper.eq("own_name", userName);
        // 默认地址置顶，再按 id 倒序
        wrapper.orderByDesc("is_default").orderByDesc("id");
        return addressMapper.selectList(wrapper);
    }

    @Override
    public Address add(Address address) {
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            clearOtherDefault(address.getOwnName(), null);
        }
        addressMapper.insert(address);
        return address;
    }

    @Override
    public boolean update(Address address) {
        if (address.getId() == null) {
            return false;
        }
        Address existing = addressMapper.selectById(address.getId());
        if (existing == null) {
            return false;
        }
        // 只允许更新自己的地址；own_name 不允许被篡改
        address.setOwnName(existing.getOwnName());
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            clearOtherDefault(existing.getOwnName(), address.getId());
        }
        return addressMapper.updateById(address) > 0;
    }

    @Override
    public boolean delete(Integer id, String userName) {
        Address existing = addressMapper.selectById(id);
        if (existing == null || !existing.getOwnName().equals(userName)) {
            return false;
        }
        return addressMapper.deleteById(id) > 0;
    }

    /** 将某用户除 excludeId 外的地址全部取消默认 */
    private void clearOtherDefault(String userName, Integer excludeId) {
        UpdateWrapper<Address> wrapper = new UpdateWrapper<>();
        wrapper.eq("own_name", userName).set("is_default", 0);
        if (excludeId != null) {
            wrapper.ne("id", excludeId);
        }
        addressMapper.update(null, wrapper);
    }
}
