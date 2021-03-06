/**
 * Copyright (c) 2000-2011 Liferay, Inc. All rights reserved.
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

package com.liferay.portlet.ratings.service.impl;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.model.User;
import com.liferay.portal.security.permission.ActionKeys;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.asset.model.AssetEntry;
import com.liferay.portlet.blogs.model.BlogsEntry;
import com.liferay.portlet.blogs.model.BlogsStatsUser;
import com.liferay.portlet.ratings.model.RatingsEntry;
import com.liferay.portlet.ratings.model.RatingsStats;
import com.liferay.portlet.ratings.service.base.RatingsEntryLocalServiceBaseImpl;
import com.liferay.portlet.social.model.SocialActivityConstants;

import java.util.Date;
import java.util.List;

/**
 * @author Brian Wing Shun Chan
 * @author Zsolt Berentey
 */
public class RatingsEntryLocalServiceImpl
	extends RatingsEntryLocalServiceBaseImpl {

	public void deleteEntry(long userId, String className, long classPK)
		throws PortalException, SystemException {

		// Entry

		long classNameId = PortalUtil.getClassNameId(className);

		RatingsEntry entry = ratingsEntryPersistence.fetchByU_C_C(
			userId, classNameId, classPK);

		if (entry == null) {
			return;
		}

		double oldScore = entry.getScore();

		ratingsEntryPersistence.removeByU_C_C(userId, classNameId, classPK);

		// Stats

		RatingsStats stats = ratingsStatsLocalService.getStats(
			className, classPK);

		int totalEntries = stats.getTotalEntries() - 1;
		double totalScore = stats.getTotalScore() - oldScore;
		double averageScore = 0;

		if (totalEntries > 0) {
			averageScore = totalScore / totalEntries;
		}

		stats.setTotalEntries(totalEntries);
		stats.setTotalScore(totalScore);
		stats.setAverageScore(averageScore);

		ratingsStatsPersistence.update(stats, false);

		// Social

		socialEquityLogLocalService.deactivateEquityLogs(
			userId, className, classPK, ActionKeys.ADD_VOTE,
			StringPool.BLANK);
	}

	public RatingsEntry fetchEntry(long userId, String className, long classPK)
		throws SystemException {

		long classNameId = PortalUtil.getClassNameId(className);

		return ratingsEntryPersistence.fetchByU_C_C(
			userId, classNameId, classPK);
	}

	public List<RatingsEntry> getEntries(
			long userId, String className, List<Long> classPKs)
		throws SystemException {

		long classNameId = PortalUtil.getClassNameId(className);

		return ratingsEntryFinder.findByU_C_C(userId, classNameId, classPKs);
	}

	public List<RatingsEntry> getEntries(String className, long classPK)
		throws SystemException {

		long classNameId = PortalUtil.getClassNameId(className);

		return ratingsEntryPersistence.findByC_C(classNameId, classPK);
	}

	public List<RatingsEntry> getEntries(
			String className, long classPK, double score)
		throws SystemException {

		long classNameId = PortalUtil.getClassNameId(className);

		return ratingsEntryPersistence.findByC_C_S(classNameId, classPK, score);
	}

	public int getEntriesCount(String className, long classPK, double score)
		throws SystemException {

		long classNameId = PortalUtil.getClassNameId(className);

		return ratingsEntryPersistence.countByC_C_S(
			classNameId, classPK, score);
	}

	public RatingsEntry getEntry(long userId, String className, long classPK)
		throws PortalException, SystemException {

		long classNameId = PortalUtil.getClassNameId(className);

		return ratingsEntryPersistence.findByU_C_C(
			userId, classNameId, classPK);
	}

	public RatingsEntry updateEntry(
			long userId, String className, long classPK, double score,
			ServiceContext serviceContext)
		throws PortalException, SystemException {

		// Entry

		boolean newEntry = false;

		long classNameId = PortalUtil.getClassNameId(className);
		double oldScore = 0;
		Date now = new Date();

		RatingsEntry entry = ratingsEntryPersistence.fetchByU_C_C(
			userId, classNameId, classPK);

		if (entry != null) {
			oldScore = entry.getScore();

			entry.setModifiedDate(serviceContext.getModifiedDate(now));
			entry.setScore(score);

			ratingsEntryPersistence.update(entry, false);

			// Stats

			RatingsStats stats = ratingsStatsLocalService.getStats(
				className, classPK);

			stats.setTotalScore(stats.getTotalScore() - oldScore + score);
			stats.setAverageScore(
				stats.getTotalScore() / stats.getTotalEntries());

			ratingsStatsPersistence.update(stats, false);
		}
		else {
			newEntry = true;

			User user = userPersistence.findByPrimaryKey(userId);

			long entryId = counterLocalService.increment();

			entry = ratingsEntryPersistence.create(entryId);

			entry.setCompanyId(user.getCompanyId());
			entry.setUserId(user.getUserId());
			entry.setUserName(user.getFullName());
			entry.setCreateDate(serviceContext.getCreateDate(now));
			entry.setModifiedDate(serviceContext.getModifiedDate(now));
			entry.setClassNameId(classNameId);
			entry.setClassPK(classPK);
			entry.setScore(score);

			ratingsEntryPersistence.update(entry, false);

			// Stats

			RatingsStats stats = ratingsStatsLocalService.getStats(
				className, classPK);

			stats.setTotalEntries(stats.getTotalEntries() + 1);
			stats.setTotalScore(stats.getTotalScore() + score);
			stats.setAverageScore(
				stats.getTotalScore() / stats.getTotalEntries());

			ratingsStatsPersistence.update(stats, false);
		}

		// Blogs entry

		if (className.equals(BlogsEntry.class.getName())) {
			BlogsEntry blogsEntry = blogsEntryPersistence.findByPrimaryKey(
				classPK);

			BlogsStatsUser blogsStatsUser =
				blogsStatsUserLocalService.getStatsUser(
					blogsEntry.getGroupId(), blogsEntry.getUserId());

			int ratingsTotalEntries = blogsStatsUser.getRatingsTotalEntries();
			double ratingsTotalScore = blogsStatsUser.getRatingsTotalScore();
			double ratingsAverageScore =
				blogsStatsUser.getRatingsAverageScore();

			if (newEntry) {
				ratingsTotalEntries++;
				ratingsTotalScore += score;
			}
			else {
				ratingsTotalScore = ratingsTotalScore - oldScore + score;
			}

			ratingsAverageScore = ratingsTotalScore / ratingsTotalEntries;

			blogsStatsUser.setRatingsTotalEntries(ratingsTotalEntries);
			blogsStatsUser.setRatingsTotalScore(ratingsTotalScore);
			blogsStatsUser.setRatingsAverageScore(ratingsAverageScore);

			blogsStatsUserPersistence.update(blogsStatsUser, false);
		}

		// Social

		AssetEntry assetEntry = assetEntryLocalService.fetchEntry(
			className, classPK);

		if (assetEntry != null) {
			socialActivityLocalService.addActivity(
				userId, assetEntry.getGroupId(), className, classPK,
				SocialActivityConstants.TYPE_ADD_VOTE, StringPool.BLANK, 0);
		}

		socialEquityLogLocalService.addEquityLogs(
			userId, className, classPK, ActionKeys.ADD_VOTE, StringPool.BLANK);

		return entry;
	}

}