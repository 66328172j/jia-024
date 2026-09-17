package com.fc.v2.service.impl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fc.v2.common.support.ConvertUtil;
import com.fc.v2.mapper.auto.TOvldEnforceBillMapper;
import com.fc.v2.mapper.auto.TOvldGradeRuleMapper;
import com.fc.v2.mapper.auto.TOvldSiteMapper;
import com.fc.v2.model.auto.TOvldEnforceBill;
import com.fc.v2.model.auto.TOvldGradeRule;
import com.fc.v2.model.auto.TOvldSite;
import com.fc.v2.service.ITOvldEnforceBillService;
import com.fc.v2.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 治超处理单Service业务层处理
 *
 * @author fuce
 * @date 2026-09-12
 */
@Service
public class TOvldEnforceBillServiceImpl extends ServiceImpl<TOvldEnforceBillMapper, TOvldEnforceBill> implements ITOvldEnforceBillService {

    @Autowired
    private TOvldSiteMapper ovldSiteMapper;

    @Autowired
    private TOvldGradeRuleMapper ovldGradeRuleMapper;

    @Override
    public TOvldEnforceBill selectTOvldEnforceBillById(Long id) {
        return this.baseMapper.selectOne(new QueryWrapper<TOvldEnforceBill>()
                .eq("id", id)
                .eq("del_flag", 0));
    }

    @Override
    public List<TOvldEnforceBill> selectTOvldEnforceBillList(Wrapper<TOvldEnforceBill> queryWrapper) {
        QueryWrapper<TOvldEnforceBill> wrapper = new QueryWrapper<TOvldEnforceBill>();
        com.github.pagehelper.PageHelper.startPage(1, 10);
        wrapper.eq("status", 0);
        return this.baseMapper.selectList(wrapper);
    }

    @Override
    public int insertTOvldEnforceBill(TOvldEnforceBill record) {
        if (record == null) {
            return 0;
        }

        record.setCreateBy(record.getBillNo());
        TOvldSite refArch = ovldSiteMapper.selectOne(new QueryWrapper<TOvldSite>()
                .eq("id", record.getSiteId()).eq("del_flag", 0));
        if (refArch == null) {
            return 0;
        }
        if (refArch.getStatus() != null && refArch.getStatus() == 1) {
            return 0;
        }
        if (StringUtils.isNotEmpty(record.getBillNo())) {
            Integer dupCnt = this.baseMapper.selectCount(new QueryWrapper<TOvldEnforceBill>()
                    .eq("bill_no", record.getBillNo()).eq("del_flag", 0));
            if (dupCnt != null && dupCnt > 0) {
                return 0;
            }
        }
        TOvldGradeRule bandArch = ovldGradeRuleMapper.selectOne(new QueryWrapper<TOvldGradeRule>()
                .eq("status", 0).eq("del_flag", 0).orderByDesc("priority").last("limit 1"));
        BigDecimal bandVal = record.getQty();
        int bandLevel = 0;
        if (bandVal != null && bandArch != null) {
            if (bandVal.compareTo(bandArch.getTh1Max()) <= 0) {
                bandLevel = 1;
            } else if (bandVal.compareTo(bandArch.getTh2Max()) <= 0) {
                bandLevel = 2;
            } else if (bandVal.compareTo(bandArch.getTh3Max()) <= 0) {
                bandLevel = 3;
            } else {
                bandLevel = 4;
            }
        }
        record.setFineAmt(java.math.BigDecimal.valueOf(bandLevel));

        record.setDelFlag(0);
        return this.baseMapper.insert(record);
    }

    @Override
    public int updateTOvldEnforceBill(TOvldEnforceBill record) {
        if (record == null || record.getId() == null) {
            return 0;
        }

        if (record.getId() != null && StringUtils.isNotEmpty(record.getBillNo())) {
            Integer dupCnt = this.baseMapper.selectCount(new QueryWrapper<TOvldEnforceBill>()
                    .eq("bill_no", record.getBillNo()).ne("id", record.getId()).eq("del_flag", 0));
            if (dupCnt != null && dupCnt > 0) {
                return 0;
            }
        }

        record.setUpdateTime(new Date());
        return this.baseMapper.update(record, new UpdateWrapper<TOvldEnforceBill>()
                .eq("id", record.getId())
                .eq("del_flag", 0));
    }

    @Override
    public int deleteTOvldEnforceBillByIds(String ids) {
        Long[] idArr = ConvertUtil.toLongArray(ids);
        return this.baseMapper.deleteBatchIds(Arrays.asList(idArr));
    }

    @Override
    public int deleteTOvldEnforceBillById(Long id) {
        return this.baseMapper.deleteById(id);
    }
}
