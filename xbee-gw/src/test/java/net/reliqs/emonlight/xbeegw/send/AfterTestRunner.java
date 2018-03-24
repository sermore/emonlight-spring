package net.reliqs.emonlight.xbeegw.send;

import java.io.IOException;

public class AfterTestRunner {

    private boolean doTest = true;
    private Runnable testRunner;

    public void onInit() throws IOException {
        //        AbstractServiceTest.postCount = 0;
        //        if (AbstractServiceTest.runCount == 0) {
        //            Files.deleteIfExists(Paths.get("FakeService.dat"));
        //        }
    }

    public void onClose() {
        if (doTest) {
            System.out.println("ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ!!!");
            testRunner.run();
        }
        //        ObjStoreToFile<LinkedList<StoreData>>
        //                s = new ObjStoreToFile<>("FakeService.dat", AbstractServiceTest.runCount > 0);
        //        List<LinkedList<StoreData>> res = s.read();
        //        assertThat(res.size(), is(2));
        //        assertThat(res.get(0).size(), is(AbstractServiceTest.runCount > 0 ? 3 : 2));
        //        MatcherAssert.assertThat(res.get(1).size(), Matchers.is(AbstractServiceTest.runCount));
        //        MatcherAssert.assertThat(AbstractServiceTest.postCount, Matchers.is(AbstractServiceTest.runCount + 1));
        //        AbstractServiceTest.results[AbstractServiceTest.runCount] = true;
        //        AbstractServiceTest.runCount++;
        //            if (runCount++ == 0) {
        //                saveOnCloseResult = true; // res.size() == 2 && res.get(0).size() == 2 && res.get(1).size() == 0 && postCount == 1;
        //            } else {
        //                saveOnCloseWithErrorsResult =  res.size() == 2 && res.get(0).size() == 2 && res.get(1).size() == 1 && postCount == 1;
        //            }
    }

    public boolean isDoTest() {
        return doTest;
    }

    public void setDoTest(boolean doTest) {
        this.doTest = doTest;
    }

    public Runnable getTestRunner() {
        return testRunner;
    }

    public void setTestRunner(Runnable testRunner) {
        this.testRunner = testRunner;
    }

}
