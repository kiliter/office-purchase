package com.office.purchase.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.office.purchase.common.RoleConst;
import com.office.purchase.entity.Goods;
import com.office.purchase.entity.Notice;
import com.office.purchase.entity.User;
import com.office.purchase.mapper.GoodsMapper;
import com.office.purchase.mapper.NoticeMapper;
import com.office.purchase.mapper.UserMapper;
import com.office.purchase.service.impl.UserServiceImpl;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 表为空时写入论文中的演示账号、商品和公告。
 * 已经导入 SQL 脚本时不会重复插入，也不会覆盖已修改的密码。
 */
@Component
public class DemoDataInitializer implements CommandLineRunner {

    @Resource
    private UserMapper userMapper;

    @Resource
    private GoodsMapper goodsMapper;

    @Resource
    private NoticeMapper noticeMapper;

    @Resource
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * 按用户、商品、公告的顺序补齐空表。
     */
    @Override
    public void run(String... args) {
        if (userMapper.selectCount(new QueryWrapper<User>()) == 0) {
            insertUser("admin", "系统管理员", RoleConst.ADMIN, "13800000001");
            insertUser("audit01", "采购审核员", RoleConst.AUDIT, "13800000002");
            insertUser("staff01", "普通员工", RoleConst.STAFF, "13800000003");
        }
        if (goodsMapper.selectCount(new QueryWrapper<Goods>()) == 0) {
            insertGoods("A4打印纸", "纸张文具", "70g 500张/包", "25.00", 200);
            insertGoods("中性签字笔", "书写工具", "0.5mm 黑色", "2.50", 500);
            insertGoods("牛皮档案袋", "纸张文具", "A4 10个/包", "12.00", 120);
            insertGoods("U盘", "存储设备", "32GB", "49.00", 40);
            insertGoods("无线鼠标", "电脑配件", "2.4G", "69.00", 30);
            insertGoods("便利贴", "纸张文具", "76mm 黄色", "6.50", 180);
            insertGoods("订书机", "桌面用品", "12号钉", "15.00", 60);
            insertGoods("抽杆文件夹", "纸张文具", "A4", "8.00", 150);
        }
        if (noticeMapper.selectCount(new QueryWrapper<Notice>()) == 0) {
            insertNotice("办公用品采购系统启用说明",
                    "请通过本系统提交采购申请。提交后由采购审核人员审批，通过后自动生成订单。管理员在订单管理中更新到货进度。当前版本不自动扣减库存，库存由管理员在商品管理中维护。");
            insertNotice("申请被驳回后如何处理",
                    "员工可在“我的申请”中查看驳回意见，修改采购数量和申请理由后重新提交。重新提交后状态回到待审批。");
        }
    }

    /**
     * 写入一个演示用户，密码固定为 123456 的密文。
     */
    private void insertUser(String username, String realName, String role, String phone) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(UserServiceImpl.DEFAULT_PASSWORD));
        user.setRealName(realName);
        user.setRole(role);
        user.setPhone(phone);
        user.setCreateTime(new Date());
        userMapper.insert(user);
    }

    /**
     * 写入一条办公用品档案。
     */
    private void insertGoods(String name, String type, String spec, String price, int stock) {
        Goods goods = new Goods();
        goods.setGoodsName(name);
        goods.setGoodsType(type);
        goods.setSpec(spec);
        goods.setPrice(new BigDecimal(price));
        goods.setStock(stock);
        goods.setCreateTime(new Date());
        goodsMapper.insert(goods);
    }

    /**
     * 写入一条系统公告。
     */
    private void insertNotice(String title, String content) {
        Notice notice = new Notice();
        notice.setTitle(title);
        notice.setContent(content);
        notice.setPublishTime(new Date());
        noticeMapper.insert(notice);
    }
}
