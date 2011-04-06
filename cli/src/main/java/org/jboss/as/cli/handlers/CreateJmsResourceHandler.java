/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as.cli.handlers;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jboss.as.cli.CommandContext;
import org.jboss.as.cli.Util;
import org.jboss.as.cli.operation.impl.DefaultOperationRequestBuilder;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;

/**
 *
 * @author Alexey Loubyansky
 */
public class CreateJmsResourceHandler extends CommandHandlerWithHelp {

    public CreateJmsResourceHandler() {
        super("create-jms-resource");
    }

    /* (non-Javadoc)
     * @see org.jboss.as.cli.handlers.CommandHandlerWithHelp#doHandle(org.jboss.as.cli.CommandContext)
     */
    @Override
    protected void doHandle(CommandContext ctx) {

        ModelControllerClient client = ctx.getModelControllerClient();
        if(client == null) {
            ctx.printLine("The controller client isn't available, make sure you are connected.");
            return;
        }

        if(!ctx.hasArguments()) {
            ctx.printLine("Arguments are missing");
        }

        //String target = null;
        String restype = null;
        //String description = null;
        String propsStr = null;
        //boolean enabled = false;
        String jndiName = null;

        String[] args = ctx.getCommandArguments().split("\\s+");
        int i = 0;
        while(i < args.length) {
            String arg = args[i++];
            if(arg.equals("--restype")) {
                if(i < args.length) {
                    restype = args[i++];
                }
            } else if(arg.equals("--target")) {
//                if(i < args.length) {
//                    target = args[i++];
//                }
            } else if(arg.equals("--description")) {
//                if(i < args.length) {
//                    restype = args[i++];
//                }
            } else if(arg.equals("--property")) {
                if (i < args.length) {
                    propsStr = args[i++];
                }
            } else if(arg.equals("--enabled")) {
//                if (i < args.length) {
//                    enabled = Boolean.parseBoolean(args[i++]);
//                }
            } else {
                jndiName = arg;
            }
        }

        if(restype == null) {
            ctx.printLine("Required parameter --restype is missing.");
            return;
        }

        if(jndiName == null) {
            ctx.printLine("JNDI name is missing.");
            return;
        }

        String name = null;
        final Map<String, String> props;
        if(propsStr != null) {
            props = new HashMap<String, String>();
            String[] propsArr = propsStr.split(":");
            for(String prop : propsArr) {
                int equalsIndex = prop.indexOf('=');
                if(equalsIndex < 0 || equalsIndex == prop.length() - 1) {
                    ctx.printLine("Failed to parse property '" + prop + "'");
                    return;
                }

                String propName = prop.substring(0, equalsIndex).trim();
                String propValue = prop.substring(equalsIndex + 1).trim();
                if(propName.isEmpty()) {
                    ctx.printLine("Failed to parse property '" + prop + "'");
                    return;
                }

                if(propName.equals("imqDestinationName") ||propName.equalsIgnoreCase("name")) {
                    name = propValue;
                } else if("ClientId".equals(propName)) {
                    props.put("client-id", propValue);
                }
            }
        } else {
            props = Collections.emptyMap();
        }

        if(name == null) {
            name = jndiName.replace('/', '_');
        }

        if(restype.equals("javax.jms.Queue")) {

            DefaultOperationRequestBuilder builder = new DefaultOperationRequestBuilder();
            builder.addNode("subsystem", "jms");
            builder.addNode("queue", name);
            builder.setOperationName("add");
            builder.getModelNode().get("entries").add(jndiName);

            for(String prop : props.keySet()) {
                builder.addProperty(prop, props.get(prop));
            }

            final ModelNode result;
            try {
                ModelNode request = builder.buildRequest();
                result = client.execute(request);
            } catch (Exception e) {
                ctx.printLine("Failed to perform operation: " + e.getLocalizedMessage());
                return;
            }

            if (!Util.isSuccess(result)) {
                ctx.printLine(Util.getFailureDescription(result));
                return;
            }

            ctx.printLine("Created queue " + name);

        } else if(restype.equals("javax.jms.Topic")) {

            DefaultOperationRequestBuilder builder = new DefaultOperationRequestBuilder();
            builder.addNode("subsystem", "jms");
            builder.addNode("topic", name);
            builder.setOperationName("add");
            builder.getModelNode().get("entries").add(jndiName);

            for(String prop : props.keySet()) {
                builder.addProperty(prop, props.get(prop));
            }

            final ModelNode result;
            try {
                ModelNode request = builder.buildRequest();
                result = client.execute(request);
            } catch (Exception e) {
                ctx.printLine("Failed to perform operation: " + e.getLocalizedMessage());
                return;
            }

            if (!Util.isSuccess(result)) {
                ctx.printLine(Util.getFailureDescription(result));
                return;
            }

            ctx.printLine("Created topic " + name);

        } else if(restype.equals("javax.jms.ConnectionFactory") ||
                restype.equals("javax.jms.TopicConnectionFactory") ||
                restype.equals("javax.jms.QueueConnectionFactory")) {

            DefaultOperationRequestBuilder builder = new DefaultOperationRequestBuilder();
            builder.addNode("subsystem", "jms");
            builder.addNode("connection-factory", name);
            builder.setOperationName("add");
            builder.getModelNode().get("entries").add(jndiName);

            for(String prop : props.keySet()) {
                builder.addProperty(prop, props.get(prop));
            }

            final ModelNode result;
            try {
                ModelNode request = builder.buildRequest();
                result = client.execute(request);
            } catch (Exception e) {
                ctx.printLine("Failed to perform operation: " + e.getLocalizedMessage());
                return;
            }

            if (!Util.isSuccess(result)) {
                ctx.printLine(Util.getFailureDescription(result));
                return;
            }

            ctx.printLine("Created connection factory " + name);

        } else {
            ctx.printLine("Resource type " + restype + " isn't supported.");
            return;
        }
    }
}