/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.distsql.parser.api;

import org.apache.shardingsphere.distsql.parser.segment.DataSourceSegment;
import org.apache.shardingsphere.distsql.parser.segment.rdl.EncryptColumnSegment;
import org.apache.shardingsphere.distsql.parser.segment.rdl.EncryptRuleSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.impl.AlterEncryptRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.AddResourceStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateEncryptRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.DropResourceStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.impl.DropEncryptRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.impl.ShowEncryptRulesStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

// TODO use Parameterized + XML instead of static test
public final class DistSQLStatementParserEngineTest {
    
    private static final String ADD_RESOURCE_SINGLE_WITHOUT_PASSWORD = "ADD RESOURCE ds_0(HOST=127.0.0.1,PORT=3306,DB=test0,USER=ROOT);";
    
    private static final String ADD_RESOURCE_SINGLE_WITH_PASSWORD = "ADD RESOURCE ds_0(HOST=127.0.0.1,PORT=3306,DB=test0,USER=ROOT,PASSWORD=123456);";
    
    private static final String ADD_RESOURCE_MULTIPLE = "ADD RESOURCE ds_0(HOST=127.0.0.1,PORT=3306,DB=test0,USER=ROOT,PASSWORD=123456),"
            + "ds_1(HOST=127.0.0.1,PORT=3306,DB=test1,USER=ROOT,PASSWORD=123456);";
    
    private static final String DROP_RESOURCE = "DROP RESOURCE ds_0,ds_1";
    
    private static final String CREATE_ENCRYPT_RULE = "CREATE ENCRYPT RULE t_encrypt ("
            + "COLUMNS("
            + "(NAME=user_id,PLAIN=user_plain,CIPHER=user_cipher,TYPE(NAME=AES,PROPERTIES('aes-key-value'='123456abc'))),"
            + "(NAME=order_id, CIPHER =order_cipher,TYPE(NAME=MD5))"
            + "))";
    
    private static final String ALTER_ENCRYPT_RULE = "ALTER ENCRYPT RULE t_encrypt ("
            + "COLUMNS("
            + "(NAME=user_id,PLAIN=user_plain,CIPHER=user_cipher,TYPE(NAME=AES,PROPERTIES('aes-key-value'='123456abc'))),"
            + "(NAME=order_id, CIPHER =order_cipher,TYPE(NAME=MD5))"
            + "))";
    
    private static final String DROP_ENCRYPT_RULE = "DROP ENCRYPT RULE t_encrypt,t_encrypt_order";
    
    private static final String SHOW_ENCRYPT_RULES = "SHOW ENCRYPT RULES FROM encrypt_db";
    
    private static final String SHOW_ENCRYPT_TABLE_RULE = "SHOW ENCRYPT TABLE RULE t_encrypt FROM encrypt_db";
    
    private final DistSQLStatementParserEngine engine = new DistSQLStatementParserEngine();
    
    @Test
    public void assertParseAddSingleResourceWithoutPassword() {
        SQLStatement sqlStatement = engine.parse(ADD_RESOURCE_SINGLE_WITHOUT_PASSWORD);
        assertTrue(sqlStatement instanceof AddResourceStatement);
        assertThat(((AddResourceStatement) sqlStatement).getDataSources().size(), is(1));
        DataSourceSegment dataSourceSegment = ((AddResourceStatement) sqlStatement).getDataSources().iterator().next();
        assertThat(dataSourceSegment.getName(), is("ds_0"));
        assertThat(dataSourceSegment.getHostName(), is("127.0.0.1"));
        assertThat(dataSourceSegment.getPort(), is("3306"));
        assertThat(dataSourceSegment.getDb(), is("test0"));
        assertThat(dataSourceSegment.getUser(), is("ROOT"));
    }
    
    @Test
    public void assertParseAddSingleResourceWithPassword() {
        SQLStatement sqlStatement = engine.parse(ADD_RESOURCE_SINGLE_WITH_PASSWORD);
        assertTrue(sqlStatement instanceof AddResourceStatement);
        assertThat(((AddResourceStatement) sqlStatement).getDataSources().size(), is(1));
        DataSourceSegment dataSourceSegment = ((AddResourceStatement) sqlStatement).getDataSources().iterator().next();
        assertThat(dataSourceSegment.getName(), is("ds_0"));
        assertThat(dataSourceSegment.getHostName(), is("127.0.0.1"));
        assertThat(dataSourceSegment.getPort(), is("3306"));
        assertThat(dataSourceSegment.getDb(), is("test0"));
        assertThat(dataSourceSegment.getUser(), is("ROOT"));
        assertThat(dataSourceSegment.getPassword(), is("123456"));
    }
    
    @Test
    public void assertParseAddMultipleResources() {
        SQLStatement sqlStatement = engine.parse(ADD_RESOURCE_MULTIPLE);
        assertTrue(sqlStatement instanceof AddResourceStatement);
        assertThat(((AddResourceStatement) sqlStatement).getDataSources().size(), is(2));
        List<DataSourceSegment> dataSourceSegments = new ArrayList<>(((AddResourceStatement) sqlStatement).getDataSources());
        DataSourceSegment dataSourceSegment = dataSourceSegments.get(0);
        assertThat(dataSourceSegment.getName(), is("ds_0"));
        assertThat(dataSourceSegment.getHostName(), is("127.0.0.1"));
        assertThat(dataSourceSegment.getPort(), is("3306"));
        assertThat(dataSourceSegment.getDb(), is("test0"));
        assertThat(dataSourceSegment.getUser(), is("ROOT"));
        assertThat(dataSourceSegment.getPassword(), is("123456"));
        dataSourceSegment = dataSourceSegments.get(1);
        assertThat(dataSourceSegment.getName(), is("ds_1"));
        assertThat(dataSourceSegment.getHostName(), is("127.0.0.1"));
        assertThat(dataSourceSegment.getPort(), is("3306"));
        assertThat(dataSourceSegment.getDb(), is("test1"));
        assertThat(dataSourceSegment.getUser(), is("ROOT"));
        assertThat(dataSourceSegment.getPassword(), is("123456"));
    }
    
    @Test
    public void assertParseDropResource() {
        SQLStatement sqlStatement = engine.parse(DROP_RESOURCE);
        assertTrue(sqlStatement instanceof DropResourceStatement);
        assertThat(((DropResourceStatement) sqlStatement).getNames().size(), is(2));
        assertTrue(((DropResourceStatement) sqlStatement).getNames().containsAll(Arrays.asList("ds_0", "ds_1")));
    }
    
    @Test
    public void assertParseCreateEncryptRule() {
        SQLStatement sqlStatement = engine.parse(CREATE_ENCRYPT_RULE);
        assertTrue(sqlStatement instanceof CreateEncryptRuleStatement);
        CreateEncryptRuleStatement createEncryptRuleStatement = (CreateEncryptRuleStatement) sqlStatement;
        assertThat(createEncryptRuleStatement.getRules().size(), is(1));
        EncryptRuleSegment encryptRuleSegment = createEncryptRuleStatement.getRules().iterator().next();
        assertThat(encryptRuleSegment.getTableName(), is("t_encrypt"));
        assertThat(encryptRuleSegment.getColumns().size(), is(2));
        List<EncryptColumnSegment> encryptColumnSegments = new ArrayList<>(encryptRuleSegment.getColumns());
        assertThat(encryptColumnSegments.get(0).getName(), is("user_id"));
        assertThat(encryptColumnSegments.get(0).getCipherColumn(), is("user_cipher"));
        assertThat(encryptColumnSegments.get(0).getPlainColumn(), is("user_plain"));
        assertThat(encryptColumnSegments.get(0).getEncryptor().getAlgorithmName(), is("AES"));
        assertThat(encryptColumnSegments.get(0).getEncryptor().getAlgorithmProps().get("aes-key-value"), is("123456abc"));
        assertThat(encryptColumnSegments.get(1).getName(), is("order_id"));
        assertThat(encryptColumnSegments.get(1).getCipherColumn(), is("order_cipher"));
        assertThat(encryptColumnSegments.get(1).getEncryptor().getAlgorithmName(), is("MD5"));
    }
    
    @Test
    public void assertParseAlterEncryptRule() {
        SQLStatement sqlStatement = engine.parse(ALTER_ENCRYPT_RULE);
        assertTrue(sqlStatement instanceof AlterEncryptRuleStatement);
        AlterEncryptRuleStatement alterEncryptRuleStatement = (AlterEncryptRuleStatement) sqlStatement;
        assertThat(alterEncryptRuleStatement.getRules().size(), is(1));
        EncryptRuleSegment encryptRuleSegment = alterEncryptRuleStatement.getRules().iterator().next();
        assertThat(encryptRuleSegment.getTableName(), is("t_encrypt"));
        assertThat(encryptRuleSegment.getColumns().size(), is(2));
        List<EncryptColumnSegment> encryptColumnSegments = new ArrayList<>(encryptRuleSegment.getColumns());
        assertThat(encryptColumnSegments.get(0).getName(), is("user_id"));
        assertThat(encryptColumnSegments.get(0).getCipherColumn(), is("user_cipher"));
        assertThat(encryptColumnSegments.get(0).getPlainColumn(), is("user_plain"));
        assertThat(encryptColumnSegments.get(0).getEncryptor().getAlgorithmName(), is("AES"));
        assertThat(encryptColumnSegments.get(0).getEncryptor().getAlgorithmProps().get("aes-key-value"), is("123456abc"));
        assertThat(encryptColumnSegments.get(1).getName(), is("order_id"));
        assertThat(encryptColumnSegments.get(1).getCipherColumn(), is("order_cipher"));
        assertThat(encryptColumnSegments.get(1).getEncryptor().getAlgorithmName(), is("MD5"));
    }
    
    @Test
    public void assertParseDropEncryptRule() {
        SQLStatement sqlStatement = engine.parse(DROP_ENCRYPT_RULE);
        assertTrue(sqlStatement instanceof DropEncryptRuleStatement);
        assertThat(((DropEncryptRuleStatement) sqlStatement).getTables(), is(Arrays.asList("t_encrypt", "t_encrypt_order")));
    }
    
    @Test
    public void assertParseShowEncryptRules() {
        SQLStatement sqlStatement = engine.parse(SHOW_ENCRYPT_RULES);
        assertTrue(sqlStatement instanceof ShowEncryptRulesStatement);
        assertNull(((ShowEncryptRulesStatement) sqlStatement).getTableName());
        assertThat(((ShowEncryptRulesStatement) sqlStatement).getSchema().get().getIdentifier().getValue(), is("encrypt_db"));
    }
    
    @Test
    public void assertParseShowEncryptTableRule() {
        SQLStatement sqlStatement = engine.parse(SHOW_ENCRYPT_TABLE_RULE);
        assertTrue(sqlStatement instanceof ShowEncryptRulesStatement);
        assertThat(((ShowEncryptRulesStatement) sqlStatement).getSchema().get().getIdentifier().getValue(), is("encrypt_db"));
        assertThat(((ShowEncryptRulesStatement) sqlStatement).getTableName(), is("t_encrypt"));
    }
}
