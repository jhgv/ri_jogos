# -*- coding: utf-8 -*-
import scrapy
import re
from slugify import slugify

class AmericanasSpider(scrapy.Spider):
    name = "americanas"
    allowed_domains = ["americanas.com.br"]
    start_urls = ['http://americanas.com.br/']

    def parse(self, response):
        title = response.xpath('//title/text()').extract_first()
        filename = '%s.html' % slugify(title)
        with open(filename, 'wb') as f:
            f.write(response.body)
        self.log('Saved file %s' % filename)
