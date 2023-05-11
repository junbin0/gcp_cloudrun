package com.example.helloworld.controller;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import net.spy.memcached.MemcachedClient;
import net.spy.memcached.internal.GetFuture;
import net.spy.memcached.internal.OperationFuture;

@RestController
public class MemoryCachedController {

	private static final String CACHE_HOST = System.getenv("CACHE_HOST");
	private static final String CACHE_PORT = System.getenv("CACHE_PORT");

	@GetMapping("/cache/put")
	String putCache(String key, String value) throws IOException, InterruptedException, TimeoutException, ExecutionException {
		MemcachedClient mcc = new MemcachedClient(new InetSocketAddress(CACHE_HOST, Integer.parseInt(CACHE_PORT)));
		
		OperationFuture<Boolean> setOp = mcc.set(key, 30 * 600, value);
		setOp.get(5, TimeUnit.SECONDS);
		System.out.println("set key " + key + " into cache with value = " + value);
		
		GetFuture<Object> getOp = mcc.asyncGet(key);
		Object myValue = getOp.get(5, TimeUnit.SECONDS);
		
		System.out.println("Async got cache " + key + " 's value = " + myValue);
		
		System.out.println("got cache " + key + " 's value = " + mcc.get(key));
		
		mcc.shutdown();
		return "successed";
	}

	@GetMapping("/cache/get")
	String getCache(String key) throws IOException {
		MemcachedClient mcc = new MemcachedClient(new InetSocketAddress(CACHE_HOST, Integer.parseInt(CACHE_PORT)));
		Object value = mcc.get(key);
		System.out.println("got cache " + key + " 's value = " + value);
		mcc.shutdown();
		if(value != null) {
			return value.toString();
		}
		return "null";
		
	}
}
