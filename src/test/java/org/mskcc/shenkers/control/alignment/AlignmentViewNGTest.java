/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.alignment;

import java.util.Arrays;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author sol
 */
public class AlignmentViewNGTest {

    public AlignmentViewNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    @Test
    public void testSomeMethod() {
        // TODO review the generated test code and remove the default call to fail.
        String[] a = new String[]{
            "XX-X--XXXX-X---XX-----",
            "--XXXXXXXXX-XXXXXXXXXX",};
        int l = a[0].length();
        int cl1 = a[0].replaceAll("-", "").length();
        int cl2 = a[1].replaceAll("-", "").length();
        int i1 = 0;
        int i2 = 0;
        int p1 = 0;
        int p2 = 0;
        int b = 3;
        for (int i = 0; i < l; i++) {
            if (a[0].charAt(i) != '-') {
                i1++;
            }
            if (a[1].charAt(i) != '-') {
                i2++;
            }
            if (a[0].charAt(i) != '-' && a[1].charAt(i) != '-') {
                System.err.println(i);
            }
//            System.out.println("i1 " + i1);
//            System.out.println("i2 " + i2);
            if ((i+1) % b == 0) {
                System.out.println(Arrays.asList(p1, i1));
                System.out.println(Arrays.asList(p2, i2));
//                System.out.println(Arrays.asList(p1 * 1. / cl1, i1 * 1. / cl1));
//                System.out.println(Arrays.asList(p2 * 1. / cl2, i2 * 1. / cl2));
System.out.println("");
                p1 = i1;
                p2 = i2;
            }
        }
        if(l%b != 0){
            System.out.println(Arrays.asList(p1, i1));
                System.out.println(Arrays.asList(p2, i2));
        }
        System.out.println("cl " + cl1);
    }

}
