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

import javax.management.ObjectName;

import org.jboss.deployment.DeploymentException;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.deployment.SubDeployerMBean;
import org.jboss.mx.util.ObjectNameFactory;

/**
 * JMX MBean Interface
 * 
 * @author aaronwalker - a.walker@base2services.com
 *
 */
public interface PropertiesFileDeployerMBean extends SubDeployerMBean
{
    //default object name
    public static final ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss.properties:PropertiesDeployer");

    /**
     * Returns true if this deployer can deploy the given DeploymentInfo.
     * @return True if this deployer can deploy the given DeploymentInfo.
     */
   boolean accepts(DeploymentInfo di);

    /**
     * Describe <code>init</code> method here.
     * @param di a <code>DeploymentInfo</code> value
     * @throws DeploymentException if an error occurs
     */
   void init(DeploymentInfo di) throws DeploymentException;

    /**
     * Describe <code>create</code> method here.
     * @param di a <code>DeploymentInfo</code> value
     * @throws DeploymentException if an error occurs
     */
   void create(DeploymentInfo di) throws DeploymentException;

    /**
     * The <code>start</code> method starts all the mbeans in this DeploymentInfo..
     * @param di a <code>DeploymentInfo</code> value
     * @throws DeploymentException if an error occurs
     */
   void start(DeploymentInfo di) throws DeploymentException;

    /**
     * Undeploys the package at the url string specified. This will: Undeploy packages depending on this one. Stop, destroy, and unregister all the specified mbeans Unload this package and packages this package deployed via the classpath tag. Keep track of packages depending on this one that we undeployed so that they can be redeployed should this one be redeployed.
     * @param di the <code>DeploymentInfo</code> value to stop.
     */
   void stop(DeploymentInfo di) ;

    /**
     * Describe <code>destroy</code> method here.
     * @param di a <code>DeploymentInfo</code> value
     */
   void destroy(DeploymentInfo di) ;
    
}
