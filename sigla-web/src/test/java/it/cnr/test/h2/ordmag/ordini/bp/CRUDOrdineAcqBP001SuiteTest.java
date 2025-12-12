package it.cnr.test.h2.ordmag.ordini.bp;

import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        CRUDOrdineAcqBP001.class,
        CRUDOrdineAcqBP002.class,
        CRUDOrdineAcqBP003.class,
        CRUDOrdineAcqBP004.class
})
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class CRUDOrdineAcqBP001SuiteTest { }