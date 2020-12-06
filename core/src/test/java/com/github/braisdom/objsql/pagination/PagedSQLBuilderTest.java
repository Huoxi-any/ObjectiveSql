package com.github.braisdom.objsql.pagination;

import com.github.braisdom.objsql.BeanModelDescriptor;
import com.github.braisdom.objsql.DatabaseType;
import com.github.braisdom.objsql.Databases;
import com.github.braisdom.objsql.DefaultQueryTest;
import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;

public class PagedSQLBuilderTest {

    @Test
    public void testAnsiSQLPagination() throws SQLException {
        PagedSQLBuilder sqlBuilder = Databases.getPagedSQLBuilderFactory()
                .createPagedSQLBuilder(DatabaseType.Ansi);
        Page page = Page.create(0, 30);
        String rawQuerySQL1 = "SELECT t1.id, t1.`no`, t1.name, COUNT(*) AS 'order_count' FROM members t1 \n" +
                "LEFT JOIN orders t2 ON t2.member_id = t1.id \n" +
                "GROUP BY t1.id, t1.`no`, t1.name, t2.member_id ";

        String countSQL1 = sqlBuilder.buildCountSQL(rawQuerySQL1);
        String querySQL2 = sqlBuilder.buildQuerySQL(page, rawQuerySQL1, new BeanModelDescriptor(DefaultQueryTest.Domain.class));

        Assert.assertEquals(countSQL1, "SELECT COUNT(*) AS count_ FROM " +
                "(SELECT t1.id, t1.`no`, t1.name, COUNT(*) AS 'order_count' " +
                "FROM members t1 LEFT JOIN orders t2 ON t2.member_id = t1.id " +
                "GROUP BY t1.id, t1.`no`, t1.name, t2.member_id) T");
        Assert.assertEquals(querySQL2, "SELECT t1.id, t1.`no`, t1.name, COUNT(*) AS 'order_count' " +
                "FROM members t1 LEFT JOIN orders t2 ON t2.member_id = t1.id " +
                "GROUP BY t1.id, t1.`no`, t1.name, t2.member_id OFFSET 0 ROWS FETCH NEXT 30 ROW ONLY");
    }

    @Test
    public void testMySQLPagination() throws SQLException {
        PagedSQLBuilder sqlBuilder = Databases.getPagedSQLBuilderFactory()
                .createPagedSQLBuilder(DatabaseType.MySQL);
        Page page = Page.create(0, 30);
        String rawQuerySQL1 = "SELECT t1.id, t1.`no`, t1.name, COUNT(*) AS 'order_count' FROM members t1 \n" +
                "LEFT JOIN orders t2 ON t2.member_id = t1.id \n" +
                "GROUP BY t1.id, t1.`no`, t1.name, t2.member_id ";

        String countSQL1 = sqlBuilder.buildCountSQL(rawQuerySQL1);
        String querySQL2 = sqlBuilder.buildQuerySQL(page, rawQuerySQL1, new BeanModelDescriptor(DefaultQueryTest.Domain.class));

        Assert.assertEquals(countSQL1, "SELECT COUNT(*) AS count_ FROM " +
                "(SELECT t1.id, t1.`no`, t1.name, COUNT(*) AS 'order_count' " +
                "FROM members t1 LEFT JOIN orders t2 ON t2.member_id = t1.id " +
                "GROUP BY t1.id, t1.`no`, t1.name, t2.member_id) T");
        Assert.assertEquals(querySQL2, "SELECT t1.id, t1.`no`, t1.name, COUNT(*) AS 'order_count' " +
                "FROM members t1 LEFT JOIN orders t2 ON t2.member_id = t1.id " +
                "GROUP BY t1.id, t1.`no`, t1.name, t2.member_id LIMIT 0, 30");
    }
}
