package it.cnr.test.h2.ordmag.ordini.bp;

import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        CRUDOrdineAcqBP001Test_IT.class,
        CRUDOrdineAcqBP002Test_IT.class,
        CRUDOrdineAcqBP003Test_IT.class,
        CRUDOrdineAcqBP004Test_IT.class
})
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class CRUDOrdineAcqBP001SuiteTest { }