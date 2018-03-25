package net.reliqs.emonlight.xbeegw.send;

class FakeAsyncService extends AbstractAsyncService<StoreData> {

    private long sleepTime = 0L;
    private int postCount = 0;
    private boolean generateException = false;
    private boolean result = true;

    FakeAsyncService(int maxRetries) {
        super("TEST", maxRetries);
    }

    @Override
    protected boolean send(StoreData t) {
        postCount++;
        log.debug("TEST: send!!! postCount={}, sleep={}, result={}, exc={}", postCount, sleepTime, result,
                generateException);
        try {
            if (sleepTime > 0) {
                Thread.sleep(sleepTime);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (generateException) {
            throw new RuntimeException("Error in AsyncService");
        }
        return result;
    }

    long getSleepTime() {
        return sleepTime;
    }

    void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
    }

    int getPostCount() {
        return postCount;
    }

    void setPostCount(int postCount) {
        this.postCount = postCount;
    }

    boolean isGenerateException() {
        return generateException;
    }

    void setGenerateException(boolean generateException) {
        this.generateException = generateException;
    }

    boolean isResult() {
        return result;
    }

    void setResult(boolean result) {
        this.result = result;
    }
}
