/*
 * Copyright (c) 2011-2016 The original author or authors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *      The Eclipse Public License is available at
 *      http://www.eclipse.org/legal/epl-v10.html
 *
 *      The Apache License v2.0 is available at
 *      http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package examples;

import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceReference;
import io.vertx.servicediscovery.types.EventBusService;

/**
 MicroService Provider: là bên cung cấp service
 MicroService consumer: bên dùng service
 ServiceDiscover server: bên điều phối thông tin.
 Giả định kết nối qua internet (tcp or http or event bus).
 Các vd của vertx là trường hợp đặc biệt, tất cả đều trong 1 ứng dụng vertx (hoặc Vertx cluster).
 */
// vd Provider = Eventbus Broker
public class EventBusServiceExamples {

	/**
	 * Source code này ở phía Microservice Provider
	 * Provider đăng ký (publish) service availale với Discovery
	 */
	public void exampleProvider(ServiceDiscovery discovery) {

		/**
		 * EventBusService, HttpEndpoint, MessageSource, RedisService, SQLService =>
		 *  các class này sẽ cung cấp cách tạo record giao tiếp với ServiceDicovery
		 */
		Record record = EventBusService.createRecord(
				"some-eventbus-service", // The service name
				"address", // the service address,
				"examples.MyService", // the service interface as string
				new JsonObject()
				.put("some-metadata", "some value")
				);

		/**
		 * ServiceDiscovery class ở phía Microservice Provider giúp đăng ký với Discovery Server.
		 */
		discovery.publish(record, ar -> {

			if(ar.succeeded()){

			}else { //if(ar.failed())

			}
		});
	}

	/**
	 * cách 1:
	 * 
	 * Source code này ở phía Microservice Consumer, 
	 * Consumer muốn kết nối với MicroService Provider thì nó phải hởi ServiceDiscovery Server về các Provider available.
	 *
	 */
	public void exampleConsumer1(ServiceDiscovery discovery) {

		/**
		 * dùng ServiceDiscovery class yêu cầu ServiceDiscovery Server cung cấp danh sách MicroService Provider
		 * 
		 */
		discovery.getRecord(new JsonObject().put("name", "some-eventbus-service"), ar -> {
			if (ar.succeeded() && ar.result() != null) {

				// và thông báo với ServiceDiscovery server là mình dùng service này
				Record record =   ar.result();
				ServiceReference reference = discovery.getReference(record);

				/**
				 * MyService: là class giúp service Consumer connect tới ServiceProvider để lấy thông tin. 
				 * MyService tương ứng với record trả về bởi ServiceDiscovery
				 */
				MyService service = reference.getAs(MyService.class);  //
				

				// Consumer thông báo ServiceDiscovery là mình ko dùng Service Provider này nữa
		        // ServiceDiscovery cần biết các Microservice đang tương tác với nhau thế nào?
				reference.release();
				
				// có 1 vài service dùng ConnectPool, or cachePool vì thế cần allocate và Release()
		        // vd: SQL connectpool
				// đã phần kết nối ServiceConsumer và ServiceProvider là asynchronous: eventbus
			}
		});
	}

	/**
	 * cách 2:  
	 * Source code này ở phía Microservice Consumer, 
	 * Consumer muốn kết nối với MicroService Provider thì nó phải hởi ServiceDiscovery Server về các Provider available.
	 * Source code ở vd exampleConsumer1 đc viết gọn như sau:
	 */
	public void exampleConsumer2(ServiceDiscovery discovery) {
		
		/**
		 * Proxy đc hiểu là MyService instance ở phía Consumer dùng để yêu cấu dịch vụ từ MicroService Provider
		 * Tất cả tương tác với Consumer với Discovery đc đóng gói trong EventBusService
		 * 
		 * EventBusService convert Record nhận về thành Myservice
		 */
		EventBusService.getProxy(discovery, MyService.class, ar -> {
			if (ar.succeeded()) {
				MyService service = ar.result();
				
				//... tại đây kết nối với MicroService Provider
				
				// Consumer thông báo ServiceDiscovery là mình ko dùng Service Provider này nữa
		        // ServiceDiscovery cần biết các Microservice đang tương tác với nhau thế nào?
				ServiceDiscovery.releaseServiceObject(discovery, service);
				
				// có 1 vài service dùng ConnectPool, or cachePool vì thế cần allocate và Release()
		         // vd: SQL connectpool, http2
			}
		});
	}

	/**
	 * cách 3:
	 * Source code này ở phía Microservice Consumer, 
	 * Consumer muốn kết nối với MicroService Provider thì nó phải hởi ServiceDiscovery Server về các Provider available.
	 * Source code ở vd exampleConsumer1 đc viết gọn như sau:
	 */
	public void exampleConsumer3(ServiceDiscovery discovery) {
		
		/**
		 * Proxy đc hiểu là MyService instance ở phía Consumer dùng để yêu cấu dịch vụ từ MicroService Provider
		 * Tất cả tương tác với Consumer với Discovery đc đóng gói trong EventBusService
		 * 
		 */
		EventBusService.getServiceProxyWithJsonFilter(discovery,
				new JsonObject().put("service.interface", "org.acme.MyService"), // The java interface
				MyService.class, // The expect client
				ar -> {
					if (ar.succeeded()) {
						MyService service = ar.result();

						// Dont' forget to release the service
						ServiceDiscovery.releaseServiceObject(discovery, service);
					}
				});
	}

}
