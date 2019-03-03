package fchen;

/**
 * It evaluates the performance of the protocol. To change file to be
 * outsourced, manually modify new Benchmark(
 * "D:\\test\\vs\\test2\\simplewiki-20130608-pages-articles-multistream-index.txt.bz2"
 * );
 * 
 * @author Chen, Fei (https://sites.google.com/site/chenfeiorange/)
 * @author Last updated on 04-12-2016.
 * @author Email: chenfeiorange@163.com
 * 
 */
public class PerformanceEvaluation
{

    public static void main(String[] args)
    {
	Benchmark b = new Benchmark("D:\\test\\vs\\test5\\simplewiki-20130608-pages-meta-history.xml.7z");
	b.run();
    }

}
