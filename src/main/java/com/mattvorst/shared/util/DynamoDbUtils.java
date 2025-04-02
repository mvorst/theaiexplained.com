package com.mattvorst.shared.util;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import com.mattvorst.shared.model.DynamoResultList;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;

public class DynamoDbUtils {

	public static <T> CompletableFuture<DynamoResultList<T>> queryWithPagination(DynamoDbAsyncTable<T> table, QueryEnhancedRequest request) {
		CompletableFuture<DynamoResultList<T>> resultFuture = new CompletableFuture<>();
		DynamoResultList<T> resultList = new DynamoResultList<>();

		table.query(request)
				.subscribe(new Subscriber<Page<T>>() {
					Subscription subscription;

					@Override
					public void onSubscribe(Subscription s) {
						this.subscription = s;
						s.request(1); // Request one page.
					}

					@Override
					public void onNext(Page<T> page) {
						if (resultList.getList() == null) {
							resultList.setList(new ArrayList<>());
						}
						resultList.getList().addAll(page.items());
						resultList.setLastEvaluatedKey(page.lastEvaluatedKey());
						// Since we are only interested in one page, cancel further pages.
						subscription.cancel();
						resultFuture.complete(resultList);
					}

					@Override
					public void onError(Throwable t) {
						resultFuture.completeExceptionally(t);
					}

					@Override
					public void onComplete() {
						// Complete if no pages were emitted.
						resultFuture.complete(resultList);
					}
				});

		return resultFuture;
	}

	public static <T> CompletableFuture<DynamoResultList<T>> queryWithPagination(DynamoDbAsyncIndex<T> index, QueryEnhancedRequest request) {
		CompletableFuture<DynamoResultList<T>> resultFuture = new CompletableFuture<>();
		DynamoResultList<T> resultList = new DynamoResultList<>();

		index.query(request)
				.subscribe(new Subscriber<Page<T>>() {
					Subscription subscription;

					@Override
					public void onSubscribe(Subscription s) {
						this.subscription = s;
						s.request(1); // Request one page.
					}

					@Override
					public void onNext(Page<T> page) {
						if (resultList.getList() == null) {
							resultList.setList(new ArrayList<>());
						}
						resultList.getList().addAll(page.items());
						resultList.setLastEvaluatedKey(page.lastEvaluatedKey());
						// Since we are only interested in one page, cancel further pages.
						subscription.cancel();
						resultFuture.complete(resultList);
					}

					@Override
					public void onError(Throwable t) {
						resultFuture.completeExceptionally(t);
					}

					@Override
					public void onComplete() {
						// Complete if no pages were emitted.
						resultFuture.complete(resultList);
					}
				});

		return resultFuture;
	}

	public static <T> CompletableFuture<DynamoResultList<T>> queryWithPagination(DynamoDbAsyncTable<T> table, ScanEnhancedRequest request) {
		CompletableFuture<DynamoResultList<T>> resultFuture = new CompletableFuture<>();
		DynamoResultList<T> resultList = new DynamoResultList<>();

		table.scan(request)
				.subscribe(new Subscriber<Page<T>>() {
					Subscription subscription;

					@Override
					public void onSubscribe(Subscription s) {
						this.subscription = s;
						s.request(1); // Request one page.
					}

					@Override
					public void onNext(Page<T> page) {
						if (resultList.getList() == null) {
							resultList.setList(new ArrayList<>());
						}
						resultList.getList().addAll(page.items());
						resultList.setLastEvaluatedKey(page.lastEvaluatedKey());
						// Since we are only interested in one page, cancel further pages.
						subscription.cancel();
						resultFuture.complete(resultList);
					}

					@Override
					public void onError(Throwable t) {
						resultFuture.completeExceptionally(t);
					}

					@Override
					public void onComplete() {
						// Complete if no pages were emitted.
						resultFuture.complete(resultList);
					}
				});

		return resultFuture;
	}
}
