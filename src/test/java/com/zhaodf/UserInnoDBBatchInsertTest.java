package com.zhaodf;


import com.zhaodf.entity.UserTest;
import com.zhaodf.util.NameUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@EnableAutoConfiguration
public class UserInnoDBBatchInsertTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    List<UserTest> list;

    private static final int USER_COUNT = 1000000; // 数量过大可能导致内存溢出，多运行几次


    @Test
    public void testInsert() {
        long start = System.currentTimeMillis();
        list = new ArrayList<>();
        for (int i=1; i< USER_COUNT+1; i++){
            UserTest user = new UserTest();
            user.setId(i);
            user.setName(NameUtil.getRandomName(i));
            user.setGender(((int)(10 * Math.random())) % 2  );
            String phone = getTel();
            user.setPhone(phone);
            list.add(user);
        }

        save(list);
        long end = System.currentTimeMillis();
        System.out.println("批量插入"+USER_COUNT+"条用户数据完毕，总耗时：" + (end - start) + " 毫秒");

    }

    /**
     * 必须要在数据库连接url加上 &rewriteBatchedStatements=true 来开启批处理，否则还是一条一条写入的
     * 检查IP地址
     * @param list
     */
    public void save(List<UserTest> list) {
        final List<UserTest> tempList = list;
        String sql = "insert into user_innodb(id, name, gender, phone) "
                + "values(?, ?, ?, ?)";
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Integer id = tempList.get(i).getId();
                String name =  tempList.get(i).getName();
                Integer gender = tempList.get(i).getGender();
                String phone = tempList.get(i).getPhone();

                ps.setInt(1, id);
                ps.setString(2, name);
                ps.setInt(3, gender);
                ps.setString(4, phone);
            }

            public int getBatchSize() {
                return tempList.size();
            }
        });

    }

    public static int getNum(int start,int end) {
        return (int)(Math.random()*(end-start+1)+start);
    }

    private static String[] telFirst="181,185,136,137,138,179,150,151,152,157,158,159,130,131,132,155,156,199,153,189,166".split(",");
    private static String getTel() {
        int index=getNum(0,telFirst.length-1);
        String first=telFirst[index];
        String second=String.valueOf(getNum(1,888)+10000).substring(1);
        String third=String.valueOf(getNum(1,9100)+10000).substring(1);
        return first+second+third;
    }

}
