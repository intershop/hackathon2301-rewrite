package geb.com.intershop.specs.features;

trait WaitingSupport
{
    def sleepForNSeconds(int n)
    {
        def originalMilliseconds = System.currentTimeMillis()
        waitFor(n + 1)
        {
            (System.currentTimeMillis() - originalMilliseconds) > (n * 1000)
        }
    }
}
