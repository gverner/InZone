package com.codeworks.pai.study;


/**  
 * Exponential moving average.  
 *  
 * @author Haksun Li  
 *  
 * @see <a href="http://en.wikipedia.org/wiki/Moving_average#Exponential_moving_average"> Wikipedia: Exponential moving average</a>  
 */  
public class EMA4 {  
    private final double alpha;  
    private double value = Double.NaN;  
 
    /**  
     * Construct an <tt>EMA</tt> instance.  
     *  
     * @param alpha decaying factor  
     */  
    public EMA4(double alpha) {  
        this.alpha = alpha;  
    }  
 
    /**  
     * Construct an <tt>EMA</tt> instance.  
     *  
     * <blockquote><code><pre>  
     * alpha = 2 / (numberPeriods + 1)  
     * </pre></code></blockquote>  
     *  
     * @param numberPeriods  
     */  
    public EMA4(int numberPeriods) {  
        this(2d / (numberPeriods + 1d));  
    }  
 
    public double value() {  
        return value;  
    }  
 
    public void update(Double x) {  
        value = Double.isNaN(value) ? x//initialization  
                : alpha * x + (1d - alpha) * value;//EMA definition
    }  
}  

