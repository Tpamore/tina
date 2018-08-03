package com.tpa.client.tina.enu;

/**
 * Created by tangqianfeng on 17/1/21.
 * <p>
 *     缓存类型
 *     HOLDER: 占位类型缓存，不管缓存过期与否，都会返回缓存数据给用户，直到服务器返回真实数据位置，覆盖缓存数据。
 *             HOLDER缓存类型只在Tina single请求里生效。
 *     TARGET: 目标缓存数据，只有当缓存数据没过期的时候才能返回给用户。
 * </p>
 */

public enum CacheType {
    HOLDER,
    TARGET
}
