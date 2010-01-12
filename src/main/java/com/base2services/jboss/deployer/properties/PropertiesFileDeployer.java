/**
 * Copyright 2010 base2Services All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products 
 * derived from this software without specific prior written permission."
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package com.base2services.jboss.deployer.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jboss.deployment.DeploymentException;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.deployment.DeploymentState;
import org.jboss.deployment.SubDeployer;
import org.jboss.deployment.SubDeployerSupport;

/**
 * @author aaronwalker - a.walker@base2services.com
 */
public class PropertiesFileDeployer extends SubDeployerSupport implements SubDeployer, PropertiesFileDeployerMBean
{ 

    private Map<URL, Properties> propertiesMap = new HashMap<URL, Properties>();
    
    public PropertiesFileDeployer()
    {
        initializeMainDeployer();
    }
    /**
     * Set the suffixes and relative order attributes. Those are read at subdeployer registration time by the
     * MainDeployer to update its SuffixOrder list.
     */
    protected void initializeMainDeployer()
    {
        setSuffixes(new String[] { "-config.properties" });
        setRelativeOrder(100);
    }

    /**
     * Returns true if this deployer can deploy the given DeploymentInfo.
     * 
     * @return True if this deployer can deploy the given DeploymentInfo.
     */
    public boolean accepts(DeploymentInfo di)
    {
        String urlStr = di.url.toString();
        return urlStr.endsWith("-config.properties");
    }

    /**
     * Describe <code>init</code> method here.
     * 
     * @param di
     *            a <code>DeploymentInfo</code> value
     * @throws DeploymentException
     *             if an error occurs
     */
    public void init(DeploymentInfo di) throws DeploymentException
    {
        try
        {
           if (di.watch == null)
           {
              // resolve the watch
              if (di.url.getProtocol().equals("file"))
              {
                 File file = new File(di.url.getFile());

                 // If not directory we watch the package
                 if (!file.isDirectory())
                 {
                    di.watch = di.url;
                 }
              }
              else
              {
                 // We watch the top only, no directory support
                 di.watch = di.url;
              }
           }
        }
        catch (Exception e)
        {
           log.error("failed to parse properties file: ", e);
           throw new DeploymentException(e);
        }
    }

    /**
     * Describe <code>create</code> method here.
     * 
     * @param di
     *            a <code>DeploymentInfo</code> value
     * @throws DeploymentException
     *             if an error occurs
     */
    public void create(DeploymentInfo di) throws DeploymentException
    {
        try
        {
            log.info("Deploying Properties File:" + di.url);
            
            Properties props = new Properties();
            InputStream is = new FileInputStream(di.url.getFile());
            props.load(is);
            
            propertiesMap.put(di.url, props);
            
            mergeWithSystemProperties(props);
        }
        catch (Exception e)
        {
           log.error("failed to load properties file: ", e);
           throw new DeploymentException(e);
        }

    }

    /**
     * The <code>start</code> method starts all the mbeans in this DeploymentInfo..
     * 
     * @param di
     *            a <code>DeploymentInfo</code> value
     * @throws DeploymentException
     *             if an error occurs
     */
    public void start(DeploymentInfo di) throws DeploymentException
    {
        super.start(di);
    }

    /**
     * Undeploys the package at the url string specified. This will: Undeploy packages depending on this one. Stop,
     * destroy, and unregister all the specified mbeans Unload this package and packages this package deployed via the
     * classpath tag. Keep track of packages depending on this one that we undeployed so that they can be redeployed
     * should this one be redeployed.
     * 
     * @param di
     *            the <code>DeploymentInfo</code> value to stop.
     */
    public void stop(DeploymentInfo di)
    {
        if (di.state != DeploymentState.STARTED)
        {
           log.debug("Ignoring request to stop '" + di.url + "', current state: " + di.state);
           return;
        }
        log.info("Undeploying Properties File:" + di.url);
        Properties props = propertiesMap.remove(di.url);
        if(props != null)
        {
            for(Object key: props.keySet())
            {
                String k = key.toString();
                log.debug("removing system property with key:" + k);
                System.getProperties().remove(k);
            }
            log.debug("merging all exisitng properties");
            for(Properties p: propertiesMap.values())
            {
                mergeWithSystemProperties(p);
            }
        }
    }

    /**
     * Describe <code>destroy</code> method here.
     * 
     * @param di
     *            a <code>DeploymentInfo</code> value
     */
    public void destroy(DeploymentInfo di)
    {}

    /**
     * The startService method gets the mbeanProxies for MainDeployer and ServiceController, used elsewhere.
     * 
     * @throws Exception
     *             if an error occurs
     */
    protected void startService() throws Exception
    {
        super.startService();
        log.info("Starting Property File Deployer");
    }

    protected ObjectName getObjectName(MBeanServer server, ObjectName name) throws MalformedObjectNameException
    {
        return name == null ? OBJECT_NAME : name;
    }

    private void mergeWithSystemProperties(Properties props)
    {
        for(Object key: props.keySet())
        {
            String k = key.toString();
            String v = props.getProperty(k);
            log.debug("setting system property with key=" + k + " value=" + v);
            System.setProperty(k, v);
        }
    }
}
