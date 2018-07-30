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

package io.inveasy.aspect;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import io.inveasy.annotations.Profile;
import io.inveasy.configuration.Configuration;
import io.inveasy.manager.MetricsManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class MetricsAspect
{
	private final MetricRegistry metrics = MetricsManager.getInstance().metricRegistry();
	private static final boolean metricsEnabled = Configuration.getInstance().getBoolean("monitoring.metricsEnabled", true);
	
	@Around("execution(* *(..)) && @annotation(profile)")
	public Object around(ProceedingJoinPoint joinPoint, Profile profile) throws Throwable
	{
		Object returnedValue;
		Timer.Context timerContext = null;
		
		if(metricsEnabled)
		{
			String method = profile.value();
			if(profile.value().isEmpty())
				method = joinPoint.getSignature().getName();
			
			String baseName = MetricRegistry.name(joinPoint.getSignature().getDeclaringType(), method);
			
			
			if(profile.throughput() && !profile.timings())
				metrics.meter(MetricRegistry.name(baseName, "throughput")).mark();
			else if(profile.timings())
				timerContext = metrics.timer(MetricRegistry.name(baseName, "timing")).time();
		}
		
		try
		{
			returnedValue = joinPoint.proceed();
		} finally
		{
			if(metricsEnabled && timerContext != null)
				timerContext.stop();
		}
		
		return returnedValue;
	}
}
