<script lang="ts">
    import { createEventDispatcher } from "svelte";
    import { setContext } from 'svelte';
    import { fetching, type Params } from "$utils/fetching";
    import { writable } from 'svelte/store';

    let dispatch = createEventDispatcher();
    import { validateForm, isFormValid } from "$utils/Validators";
    import { formFormat } from "$types/formFormat";

    export let style = "";
    export let dataFormat: formFormat = formFormat.JSON;
    export let url = "";
    export let method = "";
    export let addJSONData = [{}];
    export let params: Params[] = null;
    // export let errors = {};
    export let formValidators = {};
    export let id = "";

    let errors = writable({});

    async function handleSubmit(e) {
        let res;
        const formData = new FormData(e.target);

        $errors = validateForm(e, formValidators);
        if(!isFormValid($errors)){
            return;
        }
                
        dispatch("preFetch", e);

        if(dataFormat === formFormat.JSON){
            let data = formData;
            for (const [key, value] of data.entries()) {
                data[key] = value;
            }
            for(let JSONData of addJSONData){
                data = {...data, ...JSONData};
            }

            res = await fetching(url, method, params, data, true);
            dispatch("postFetch", {e, res});
            return res;
        }else{
            res = await fetching(url, method, params, formData, false);
            dispatch("postFetch", {e, res});
            return res;
        }
    }
    
    setContext("form", {errors});
</script>

<!-- @component
    A form component that handles validation and fetching
    @param {string} url - URL to fetch
    @param {string} method - method to use for fetching
    @param {string} dataFormat - "JSON" or "FormData" standard is JSON
    @param {array} addJSONData - [{key: value}] to add to the json data only if dataFormat is JSON
    @param {object} errors - {key: value} to store errors
    @param {object} formValidators - {key: value} validators for the form
    @param {number} id - id of the form
    @param {function} preFetch - dispatch event before fetching
    @param {function} postFetch - dispatch event after fetching
 -->
<form class={style} {id} action={url} {method} on:submit|preventDefault={handleSubmit}>
    <slot/>
</form>