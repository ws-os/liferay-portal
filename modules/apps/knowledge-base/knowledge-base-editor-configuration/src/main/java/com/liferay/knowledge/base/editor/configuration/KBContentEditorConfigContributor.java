/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.knowledge.base.editor.configuration;

import com.liferay.item.selector.ItemSelector;
import com.liferay.item.selector.ItemSelectorCriterion;
import com.liferay.item.selector.ItemSelectorReturnType;
import com.liferay.item.selector.criteria.URLItemSelectorReturnType;
import com.liferay.item.selector.criteria.UploadableFileReturnType;
import com.liferay.item.selector.criteria.image.criterion.ImageItemSelectorCriterion;
import com.liferay.item.selector.criteria.upload.criterion.UploadItemSelectorCriterion;
import com.liferay.item.selector.criteria.url.criterion.URLItemSelectorCriterion;
import com.liferay.knowledge.base.constants.KBPortletKeys;
import com.liferay.knowledge.base.item.selector.criterion.KBAttachmentItemSelectorCriterion;
import com.liferay.portal.kernel.editor.configuration.BaseEditorConfigContributor;
import com.liferay.portal.kernel.editor.configuration.EditorConfigContributor;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.portlet.RequestBackedPortletURLFactory;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.AggregateResourceBundleLoader;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.ResourceBundleLoader;
import com.liferay.portal.kernel.util.ResourceBundleUtil;
import com.liferay.portal.language.LanguageResources;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.portlet.ActionRequest;
import javax.portlet.PortletURL;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Roberto Díaz
 */
@Component(
	property = {
		"editor.config.key=contentEditor",
		"javax.portlet.name=" + KBPortletKeys.KNOWLEDGE_BASE_ADMIN,
		"javax.portlet.name=" + KBPortletKeys.KNOWLEDGE_BASE_ARTICLE,
		"javax.portlet.name=" + KBPortletKeys.KNOWLEDGE_BASE_DISPLAY,
		"javax.portlet.name=" + KBPortletKeys.KNOWLEDGE_BASE_SEARCH,
		"javax.portlet.name=" + KBPortletKeys.KNOWLEDGE_BASE_SECTION
	},
	service = EditorConfigContributor.class
)
public class KBContentEditorConfigContributor
	extends BaseEditorConfigContributor {

	@Override
	public void populateConfigJSONObject(
		JSONObject jsonObject, Map<String, Object> inputEditorTaglibAttributes,
		ThemeDisplay themeDisplay,
		RequestBackedPortletURLFactory requestBackedPortletURLFactory) {

		Map<String, String> fileBrowserParamsMap =
			(Map<String, String>)inputEditorTaglibAttributes.get(
				"liferay-ui:input-editor:fileBrowserParams");

		long resourcePrimKey = 0;

		if (fileBrowserParamsMap != null) {
			resourcePrimKey = GetterUtil.getLong(
				fileBrowserParamsMap.get("resourcePrimKey"));
		}

		ItemSelectorCriterion[] itemSelectorCriteria =
			new ItemSelectorCriterion[0];

		List<ItemSelectorReturnType> desiredItemSelectorReturnTypes =
			new ArrayList<>();

		desiredItemSelectorReturnTypes.add(new UploadableFileReturnType());
		desiredItemSelectorReturnTypes.add(new URLItemSelectorReturnType());

		if (resourcePrimKey != 0) {
			ItemSelectorCriterion attachmentItemSelectorCriterion =
				new KBAttachmentItemSelectorCriterion(resourcePrimKey);

			attachmentItemSelectorCriterion.setDesiredItemSelectorReturnTypes(
				desiredItemSelectorReturnTypes);

			itemSelectorCriteria = ArrayUtil.append(
				itemSelectorCriteria, attachmentItemSelectorCriterion);
		}

		ItemSelectorCriterion imageItemSelectorCriterion =
			new ImageItemSelectorCriterion();

		imageItemSelectorCriterion.setDesiredItemSelectorReturnTypes(
			desiredItemSelectorReturnTypes);

		itemSelectorCriteria = ArrayUtil.append(
			itemSelectorCriteria, imageItemSelectorCriterion);

		ItemSelectorCriterion urlItemSelectorCriterion =
			new URLItemSelectorCriterion();

		List<ItemSelectorReturnType> urlDesiredItemSelectorReturnTypes =
			new ArrayList<>();

		urlDesiredItemSelectorReturnTypes.add(new URLItemSelectorReturnType());

		urlItemSelectorCriterion.setDesiredItemSelectorReturnTypes(
			urlDesiredItemSelectorReturnTypes);

		itemSelectorCriteria = ArrayUtil.append(
			itemSelectorCriteria, urlItemSelectorCriterion);

		if (resourcePrimKey != 0) {
			PortletURL portletURL =
				requestBackedPortletURLFactory.createActionURL(
					KBPortletKeys.KNOWLEDGE_BASE_ADMIN);

			portletURL.setParameter(
				ActionRequest.ACTION_NAME, "uploadKBArticleAttachments");
			portletURL.setParameter(
				"resourcePrimKey", String.valueOf(resourcePrimKey));

			ResourceBundle resourceBundle =
				_resourceBundleLoader.loadResourceBundle(
					LocaleUtil.toLanguageId(themeDisplay.getLocale()));

			ItemSelectorCriterion uploadItemSelectorCriterion =
				new UploadItemSelectorCriterion(
					portletURL.toString(),
					ResourceBundleUtil.getString(
						resourceBundle, "article-attachments"));

			List<ItemSelectorReturnType> uploadDesiredItemSelectorReturnTypes =
				new ArrayList<>();

			uploadDesiredItemSelectorReturnTypes.add(
				new UploadableFileReturnType());

			uploadItemSelectorCriterion.setDesiredItemSelectorReturnTypes(
				uploadDesiredItemSelectorReturnTypes);

			itemSelectorCriteria = ArrayUtil.append(
				itemSelectorCriteria, uploadItemSelectorCriterion);
		}

		String namespace = GetterUtil.getString(
			inputEditorTaglibAttributes.get(
				"liferay-ui:input-editor:namespace"));
		String name = GetterUtil.getString(
			inputEditorTaglibAttributes.get("liferay-ui:input-editor:name"));

		PortletURL itemSelectorURL = _itemSelector.getItemSelectorURL(
			requestBackedPortletURLFactory, namespace + name + "selectItem",
			itemSelectorCriteria);

		jsonObject.put(
			"filebrowserImageBrowseLinkUrl", itemSelectorURL.toString());
		jsonObject.put("filebrowserImageBrowseUrl", itemSelectorURL.toString());
	}

	@Reference(unbind = "-")
	public void setItemSelector(ItemSelector itemSelector) {
		_itemSelector = itemSelector;
	}

	@Reference(
		target = "(bundle.symbolic.name=com.liferay.knowledge.base.item.selector.web)",
		unbind = "-"
	)
	protected void setResourceBundleLoader(
		ResourceBundleLoader resourceBundleLoader) {

		_resourceBundleLoader = new AggregateResourceBundleLoader(
			resourceBundleLoader, LanguageResources.RESOURCE_BUNDLE_LOADER);
	}

	private ItemSelector _itemSelector;
	private ResourceBundleLoader _resourceBundleLoader;

}