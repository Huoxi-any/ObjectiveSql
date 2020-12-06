/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.braisdom.objsql;

import com.github.braisdom.objsql.relation.Relationship;
import com.github.braisdom.objsql.relation.RelationshipNetwork;
import com.github.braisdom.objsql.util.StringUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/**
 * The default implementation of <code>Query</code> with JavaBean
 * @param <T>
 */
public class DefaultQuery<T> extends AbstractQuery<T> {

    private static final String SELECT_STATEMENT = "SELECT %s FROM %s";

    public DefaultQuery(Class<T> domainModelClass) {
        super(domainModelClass);
    }

    @Override
    public List<T> execute(Relationship... relationships) throws SQLException {
        String dataSourceName = domainModelDescriptor.getDataSourceName();
        return Databases.execute(dataSourceName, (connection, sqlExecutor) -> {
            Quoter quoter = Databases.getQuoter();
            String databaseProductName = connection.getMetaData().getDatabaseProductName();
            String tableName = quoter.quoteTableName(databaseProductName, domainModelDescriptor.getTableName());
            String sql = createQuerySQL(tableName);
            List rows = sqlExecutor.query(connection, sql, domainModelDescriptor, params);

            if (relationships.length > 0 && rows.size() > 0) {
                new RelationshipNetwork(connection, domainModelDescriptor).process(rows, relationships);
            }

            return rows;
        });
    }

    @Override
    public T queryFirst(Relationship... relationships) throws SQLException {
        List<T> results = execute(relationships);
        if (results.size() > 0) {
            return results.get(0);
        }
        return null;
    }

    @Override
    public String getQuerySQL(DatabaseType databaseType) {
        Quoter quoter = Databases.getQuoter();
        String tableName = quoter.quoteTableName(databaseType.getDatabaseProductName(),
                domainModelDescriptor.getTableName());

        return createQuerySQL(tableName);
    }

    protected String createQuerySQL(String tableName) {
        Objects.requireNonNull(tableName, "The tableName cannot be null");

        StringBuilder sql = new StringBuilder();

        projections = (projections == null || projections.length() < 0) ? "*" : projections;

        sql.append("SELECT ").append(projections).append(" FROM ").append(tableName);

        if (!StringUtil.isBlank(filter)) {
            sql.append(" WHERE ").append(filter);
        }

        if (!StringUtil.isBlank(groupBy)) {
            sql.append(" GROUP BY ").append(groupBy);
        }

        if (!StringUtil.isBlank(having)) {
            sql.append(" HAVING ").append(having);
        }

        if (!StringUtil.isBlank(orderBy)) {
            sql.append(" ORDER BY ").append(orderBy);
        }

        if (offset > -1 ) {
            sql.append(" OFFSET ").append(offset).append(" ROWS ");
        }

        if (rowCount > -1) {
            sql.append(" FETCH ").append(fetchNext ? " NEXT " : " FIRST ")
                    .append(rowCount).append(" ROWS ONLY ");
        }

        return sql.toString();
    }
}
