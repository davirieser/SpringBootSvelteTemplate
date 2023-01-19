<script lang="ts">
    import { marked } from 'marked';
    import markedKatex from 'marked-katex-extension';
	import hljs from 'highlight.js';

    marked.setOptions({
		highlight(code, {lang}) {
			if (typeof lang === 'undefined') {
				return hljs.highlightAuto(code).value;
			} else if (lang === 'nohighlight') {
				return code;
			} else {
				return hljs.highlight(lang, code).value;
			}
		}
	});
    marked.use(markedKatex());

    export let data;
    const markdown = marked.parse(data);
</script>

<svelte:head>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/katex@0.16.2/dist/katex.min.css" integrity="sha384-bYdxxUwYipFNohQlHt0bjN/LCpueqWz13HufFEV1SUatKs1cm4L6fFgCi1jT643X" crossorigin="anonymous">
</svelte:head>
<div id="divTextarea" class="overflow-clip w-full prose prose-dark break-all spanTransparent">
    {@html markdown}
</div>



<style>
    @import 'https://unpkg.com/@highlightjs/cdn-assets@10.6.0/styles/atom-one-dark.min.css';
    #divTextarea {
        -moz-appearance: textfield-multiline;
        -webkit-appearance: textarea;
    }
    .spanTransparent :where(span){
        background-color: transparent !important;
    }
</style>