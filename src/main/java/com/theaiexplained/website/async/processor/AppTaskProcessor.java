package com.theaiexplained.website.async.processor;

import com.mattvorst.shared.async.processor.TaskProcessor;
import com.theaiexplained.website.dao.ContentDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AppTaskProcessor extends TaskProcessor {
	@Autowired private ContentDao contentDao;
}
