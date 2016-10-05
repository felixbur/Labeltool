package com.felix.util;
/*
 * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public 
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this program; if not, write to the Free 
 * Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, 
 * MA  02111-1307, USA.
 */
//package no.geosoft.cc.util;



import java.util.Date;



/**
 * Class for program event timing.
 * Usage:
 *
 *   <pre>
 *   Timer timer = new Timer();
 *
 *   // do stuff
 *
 *   System.out.println (timer);  // prints time elapsed since
 *                                // object was created.
 *   </pre>
 * 
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public class TimeStopper
{
  private Date  start_;
  

  
  /**
   * Start timer.
   */
  public TimeStopper()
  {
    reset();
  }


  
  /**
   * Returns exact number of milliseconds since timer was started.
   * 
   * @return  Number of milliseconds since timer was started.
   */
  public long getTime()
  {
    Date now = new Date();
    long nMillis = now.getTime() - start_.getTime();

    return nMillis;
  }


  
  /**
   * Restarts the timer.
   */
  public void reset()
  {
    start_ = new Date();  // now    
  }


  
  /**
   * Returns a formatted string showing the elaspsed time
   * suince the instance was created.
   * 
   * @return  Formatted time string.
   */
  public String toString()
  {
    long nMillis = getTime();
    
    long nHours   = nMillis / 1000 / 60 / 60;
    nMillis -= nHours * 1000 * 60 * 60;
      
    long nMinutes = nMillis / 1000 / 60;
    nMillis -= nMinutes * 1000  * 60;

    long nSeconds = nMillis / 1000;
    nMillis -= nSeconds * 1000;
    
    StringBuffer time = new StringBuffer();
    if (nHours > 0) time.append (nHours + ":");
    if (nHours > 0 && nMinutes < 10) time.append ("0");
    time.append (nMinutes + ":");
    if (nSeconds < 10) time.append ("0");
    time.append (nSeconds);
    time.append (".");
    if (nMillis < 100) time.append ("0");
    if (nMillis <  10) time.append ("0");
    time.append (nMillis);
    
    return time.toString();
  }


  
  /**
   * Testing this class.
   * 
   * @param args  Not used.
   */
  public static void main (String[] args)
  {
    TimeStopper timer = new TimeStopper();

    for (int i = 0; i < 1000000000; i++) {
      double b = 9958.43678;
      double c = Math.sqrt (b);
      double d = c/b;
    }

    System.out.println (timer);
  }
}