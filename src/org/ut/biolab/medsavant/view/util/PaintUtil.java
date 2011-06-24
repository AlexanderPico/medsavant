/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 *
 * @author mfiume
 */
public class PaintUtil {

    private static final Color skyColor = new Color(200,235,254);
    
    public static void paintSky(Graphics g, Component c) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.white);
        g2.fillRect(0, 0, c.getWidth(), c.getHeight());
        
        GradientPaint p = new GradientPaint(0, c.getHeight()-200, Color.white, 0, c.getHeight(), 
                skyColor);
        g2.setPaint(p);
        g2.fillRect(0, 0, c.getWidth(), c.getHeight());
    }
    
}
