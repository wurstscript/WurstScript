/*
Language: None
*/

hljs.LANGUAGES.code = function(){

  return {
    defaultMode: {
      contains: [
        {
			className: 'git',
			begin: '\\$', end: '$'
		}
      ]
    }
  };
}();

