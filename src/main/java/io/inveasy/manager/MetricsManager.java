/*
 * Copyright 2018 Guillaume Gravetot
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.inveasy.manager;

import com.codahale.metrics.MetricRegistry;
import io.inveasy.configuration.Configuration;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;

import java.io.IOException;

public class MetricsManager
{
	private static final boolean metricsEnabled = Configuration.getInstance().getBoolean("monitoring.metricsEnabled", true);
	
	private static MetricsManager instance;
	
	public static MetricsManager getInstance()
	{
		if(instance == null)
			instance = new MetricsManager();
		
		return instance;
	}
	
	private final MetricRegistry metrics;
	
	private MetricsManager()
	{
		metrics = new MetricRegistry();
		
		if(metricsEnabled)
		{
			DefaultExports.initialize();
			CollectorRegistry.defaultRegistry.register(new DropwizardExports(metrics));
			try
			{
				// TODO Add a way to close the server
				HTTPServer server = new HTTPServer(Configuration.getInstance().getInt("metrics.prometheus.port", 9095));
			} catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public MetricRegistry metricRegistry()
	{
		return metrics;
	}
}
