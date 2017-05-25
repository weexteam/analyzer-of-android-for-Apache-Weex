package com.taobao.weex.analyzer.core;

import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.analyzer.core.lint.PollingVDomMonitor;
import com.taobao.weex.analyzer.core.lint.StandardVDomMonitor;
import com.taobao.weex.analyzer.core.lint.VDomController;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */
@RunWith(RobolectricTestRunner.class)
public class VDomControllerTest {

    @Mock
    private PollingVDomMonitor mPollingVDomMonitor;
    @Mock
    private StandardVDomMonitor mStandardVDomMonitor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void monitor() throws Exception {
        VDomController controller = new VDomController(mPollingVDomMonitor,mStandardVDomMonitor);
        WXSDKInstance instance = mock(WXSDKInstance.class);
        VDomController.isPollingMode = true;

        controller.monitor(instance);
        verify(mPollingVDomMonitor).monitor(instance);

        VDomController.isPollingMode = false;
        VDomController.isStandardMode = true;

        controller.monitor(instance);
        verify(mStandardVDomMonitor).monitor(instance);
    }

    @Test
    public void destroy() throws Exception {
        VDomController controller = new VDomController(mPollingVDomMonitor,mStandardVDomMonitor);
        controller.destroy();
        verify(mPollingVDomMonitor).destroy();
        verify(mStandardVDomMonitor).destroy();
    }

}