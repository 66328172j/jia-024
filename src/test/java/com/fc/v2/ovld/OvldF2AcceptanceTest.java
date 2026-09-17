package com.fc.v2.ovld;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.mapper.auto.TOvldUnloadDispatchMapper;
import com.fc.v2.model.auto.TOvldUnloadDispatch;
import com.fc.v2.service.ITOvldUnloadDispatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ovld F1 超限检测任务周期扫描与到期处理 · 功能点验收测试（scheduling-job 形状：直调周期执行，用例自播种）
 * create 侧产物，仅质检使用，不交付执行模型。
 */
@SpringBootTest
public class OvldF2AcceptanceTest {

    /** 本测试类自己插入的条目编号前缀（@BeforeEach 只清这一批，不动其它数据） */
    private static final String PFX = "SJ-";

    @javax.annotation.Resource
    private ITOvldUnloadDispatchService ovldUnloadDispatchService;

    @javax.annotation.Resource
    private TOvldUnloadDispatchMapper ovldUnloadDispatchMapper;

    @BeforeEach
    public void setUp() {
        ovldUnloadDispatchMapper.delete(new QueryWrapper<TOvldUnloadDispatch>().likeRight("item_no", PFX));
    }

    private static java.util.Date at(String s) {
        return java.sql.Timestamp.valueOf(s);
    }

    private TOvldUnloadDispatch mk(String no, String dueAt, String amount, int status) {
        TOvldUnloadDispatch r = new TOvldUnloadDispatch();
        r.setItemNo(no);
        r.setDueAt(at(dueAt));
        r.setAmount(new java.math.BigDecimal(amount));
        r.setStatus(Integer.valueOf(status));
        return r;
    }

    /** 坑1 执行窗口 */
    @Test
    public void trap1_out_of_window_should_not_run() {
        ovldUnloadDispatchMapper.insert(mk(PFX + "W1", "2026-09-14 03:00:00", "5", 0));
        assertEquals(0, ovldUnloadDispatchService.runOnce(at("2026-09-14 08:00:00")), "窗口外不应处理任何条目");
    }

    /** 坑2 到期端点 */
    @Test
    public void trap2_due_at_boundary_should_be_included() {
        ovldUnloadDispatchMapper.insert(mk(PFX + "D2", "2026-09-14 03:00:00", "5", 0));
        assertEquals(1, ovldUnloadDispatchService.runOnce(at("2026-09-14 03:00:00")),
                "恰好到期的条目应当被处理");
    }

    /** 坑3 已处理不再处理 */
    @Test
    public void trap3_processed_item_should_be_skipped() {
        ovldUnloadDispatchMapper.insert(mk(PFX + "S3A", "2026-09-14 03:00:00", "5", 0));
        ovldUnloadDispatchMapper.insert(mk(PFX + "S3B", "2026-09-14 03:00:00", "5", 1));
        assertEquals(1, ovldUnloadDispatchService.runOnce(at("2026-09-14 03:30:00")),
                "已处理过的条目不应重复处理");
    }

    /** 坑4 单条缺值跳过 */
    @Test
    public void trap4_bad_item_should_be_skipped_not_abort() {
        ovldUnloadDispatchMapper.insert(mk(PFX + "E4A", "2026-09-14 03:00:00", "5", 0));
        TOvldUnloadDispatch bad = mk(PFX + "E4B", "2026-09-14 03:00:00", "1", 0);
        bad.setAmount(null);
        ovldUnloadDispatchMapper.insert(bad);
        assertEquals(1, ovldUnloadDispatchService.runOnce(at("2026-09-14 03:30:00")),
                "单条失败不应中断整次执行");
        assertEquals(2, ovldUnloadDispatchService.selectTOvldUnloadDispatchById(bad.getId()).getStatus().intValue(),
                "失败条目的状态不符合预期");
    }

    /** 坑5 无到期条目 */
    @Test
    public void trap5_nothing_due_should_return_zero() {
        assertEquals(0, ovldUnloadDispatchService.runOnce(at("2026-09-14 03:30:00")),
                "无可处理条目时应返回 0");
    }

    /** 坑6 处理完落状态 */
    @Test
    public void trap6_processed_item_should_be_marked() {
        TOvldUnloadDispatch r = mk(PFX + "M6", "2026-09-14 03:00:00", "5", 0);
        ovldUnloadDispatchMapper.insert(r);
        ovldUnloadDispatchService.runOnce(at("2026-09-14 03:30:00"));
        assertEquals(1, ovldUnloadDispatchService.selectTOvldUnloadDispatchById(r.getId()).getStatus().intValue(),
                "处理完的条目状态没有落库");
    }
}
