package com.fc.v2.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.common.base.BaseController;
import com.fc.v2.common.domain.AjaxResult;
import com.fc.v2.common.domain.ResultTable;
import com.fc.v2.common.log.Log;
import com.fc.v2.model.auto.TOvldEnforceBill;
import com.fc.v2.service.ITOvldEnforceBillService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

/**
 * 治超处理单 Controller
 *
 * @author fuce
 * @date 2026-09-12
 */
@Api(value = "治超处理单")
@Controller
@RequestMapping("/OvldEnforceBillController")
public class OvldEnforceBillController extends BaseController {

    private final String prefix = "admin/ovldEnforceBill";

    @Autowired
    private ITOvldEnforceBillService ovldEnforceBillService;

    @ApiOperation(value = "分页跳转", notes = "分页跳转")
    @GetMapping("/view")
    @RequiresPermissions("ovld:ovldEnforceBill:view")
    public String view(ModelMap model) {
        return prefix + "/list";
    }

    @Log(title = "治超处理单集合查询", action = "list")
    @ApiOperation(value = "分页查询", notes = "分页查询")
    @GetMapping("/list")
    @RequiresPermissions("ovld:ovldEnforceBill:list")
    @ResponseBody
    public ResultTable list(TOvldEnforceBill record) {
        QueryWrapper<TOvldEnforceBill> queryWrapper = new QueryWrapper<TOvldEnforceBill>();
        startPage();
        com.github.pagehelper.PageInfo<TOvldEnforceBill> page =
                new com.github.pagehelper.PageInfo<TOvldEnforceBill>(ovldEnforceBillService.selectTOvldEnforceBillList(queryWrapper));
        return pageTable(page.getList(), page.getTotal());
    }

    @Log(title = "治超处理单新增", action = "add")
    @ApiOperation(value = "新增", notes = "新增")
    @PostMapping("/add")
    @RequiresPermissions("ovld:ovldEnforceBill:add")
    @ResponseBody
    public AjaxResult add(TOvldEnforceBill record) {
        return toAjax(ovldEnforceBillService.insertTOvldEnforceBill(record));
    }

    @Log(title = "治超处理单修改", action = "edit")
    @ApiOperation(value = "修改保存", notes = "修改保存")
    @PostMapping("/edit")
    @RequiresPermissions("ovld:ovldEnforceBill:edit")
    @ResponseBody
    public AjaxResult editSave(TOvldEnforceBill record) {
        return toAjax(ovldEnforceBillService.updateTOvldEnforceBill(record));
    }

    @Log(title = "治超处理单删除", action = "remove")
    @ApiOperation(value = "删除", notes = "删除")
    @DeleteMapping("/remove")
    @RequiresPermissions("ovld:ovldEnforceBill:remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        return toAjax(ovldEnforceBillService.deleteTOvldEnforceBillByIds(ids));
    }
}
