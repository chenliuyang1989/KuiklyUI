/*
 * Tencent is pleased to support the open source community by making KuiklyUI
 * available.
 * Copyright (C) 2025 THL A29 Limited, a Tencent company. All rights reserved.
 * Licensed under the License of KuiklyUI;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * https://github.com/Tencent-TDS/KuiklyUI/blob/main/LICENSE
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tencent.kuikly.demo.pages.app

import com.tencent.kuikly.demo.pages.base.BasePager
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewRef
import com.tencent.kuikly.core.base.attr.ImageUri
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Image
import com.tencent.kuikly.core.views.PageList
import com.tencent.kuikly.core.views.PageListView
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.tencent.kuikly.demo.pages.app.home.AppHomePage

@Page("AppTabPage")
internal class AppTabPage : BasePager() {

    private var selectedTabIndex: Int by observable(0)
    private val pageTitles = listOf<String>("首页", "视频", "发现", "消息", "我")
    private val pageIcons = listOf<String>(
        "tabbar_home.png",
        "tabbar_video.png",
        "tabbar_discover.png",
        "tabbar_message_center.png",
        "tabbar_profile.png"
    )
    private val pageIconsHighlight = listOf<String>(
        "tabbar_home_highlighted.png",
        "tabbar_video_highlighted.png",
        "tabbar_discover_highlighted.png",
        "tabbar_message_center_highlighted.png",
        "tabbar_profile_highlighted.png"
    )
    private var pageListRef : ViewRef<PageListView<*, *>>? = null

    private fun tabBar(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr {
                    height(TAB_BOTTOM_HEIGHT)
                    flexDirectionRow()
                    turboDisplayAutoUpdateEnable(false)
                    backgroundColor(Color(250, 250, 250, 1f))
                }
                for (i in 0 until ctx.pageTitles.size) {
                    View {
                        attr {
                            flex(1f)
                            allCenter()
                        }
                        event {
                            click {
                                ctx.selectedTabIndex = i
                                ctx.pageListRef?.view?.scrollToPageIndex(i)
                            }
                        }
                        Image {
                            attr {
                                val path = if (i == ctx.selectedTabIndex) ctx.pageIconsHighlight[i] else ctx.pageIcons[i]
                                src(ImageUri.pageAssets(path))
                                size(30f, 30f)
                            }
                        }
                        Text {
                            attr {
                                text(ctx.pageTitles[i])
                                color(if (i == ctx.selectedTabIndex) Color.RED else Color.BLACK)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr {
                    height(pagerData.statusBarHeight)
                }
            }

            PageList {
                ref {
                    ctx.pageListRef = it
                }
                attr {
                    flexDirectionRow()
                    pageItemWidth(pagerData.pageViewWidth)
                    pageItemHeight(pagerData.pageViewHeight - pagerData.statusBarHeight - TAB_BOTTOM_HEIGHT)
                    defaultPageIndex(0)
                    showScrollerIndicator(false)
                    scrollEnable(false)
                    keepItemAlive(true)
                }
                AppHomePage { }
                for (i in 1 until ctx.pageTitles.size) {
                    AppEmptyPage(ctx.pageTitles[i]) { }
                }
            }
            ctx.tabBar().invoke(this)
        }
    }

    companion object {
        const val TAB_BOTTOM_HEIGHT = 80f
    }
}