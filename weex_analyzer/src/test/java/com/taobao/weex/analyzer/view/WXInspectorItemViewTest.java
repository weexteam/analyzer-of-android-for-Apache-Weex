package com.taobao.weex.analyzer.view;

import com.taobao.weex.analyzer.core.inspector.view.WXInspectorItemView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */
public class WXInspectorItemViewTest {
    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void getPureValue() throws Exception {
        assertEquals("13", WXInspectorItemView.getPureValue("12.78"));
        assertEquals("12",WXInspectorItemView.getPureValue("12."));
        assertEquals("-13",WXInspectorItemView.getPureValue("-12.78"));
        assertEquals("13",WXInspectorItemView.getPureValue("12.a78"));
        assertEquals("-13",WXInspectorItemView.getPureValue("-12.78px"));
        assertEquals("12",WXInspectorItemView.getPureValue("12wx"));
        assertEquals("3",WXInspectorItemView.getPureValue("3.424223324244"));
        assertEquals("-3",WXInspectorItemView.getPureValue("-3.424223324244"));
        assertEquals("0",WXInspectorItemView.getPureValue(".424223324244"));
        assertEquals("1",WXInspectorItemView.getPureValue("0.8"));

    }

}