<script lang="ts">
	import Nav from '$components/nav.svelte';
	import SvelteToast from '$components/SvelteToast.svelte';

	import Deck from '$components/deck.svelte';
	import AdminDeck from '$components/adminDeck.svelte';
	import SubscribedDeck from '$components/subscribedDeck.svelte';
	import Spinner from '$components/Spinner.svelte';
	import EditDeckModal from './editDeckModal.svelte';


	import { fly } from 'svelte/transition';

	import { jwt } from "$stores/jwtStore";
	import { addToast, addToastByRes } from '$utils/addToToastStore';
	import { redirect } from "$utils/redirect";
	import { userPermissionsStore } from '$stores/userPermissionsStore';
	import { userSelectedDeckStore } from '$stores/userSelectedDeckStore';
  	import { fetching } from '$utils/fetching';
	import ListCardsModal from './listCardsModal.svelte';
	import PublicDecksModal from './publicDecksModal.svelte';

	$: if(!$jwt || $jwt?.expired) redirect("login");
	$: if($userPermissionsStore.includes("ADMIN")) getAllDecks();
	$: getUserDecks();
	$: getSubscribedDecks();

	let allDecks = [];
	let userDecks = [];
	let subscribedDecks = [];

	let showEditDeckModal = false;
	let selectedDeck = null;
	let listCards = false;
	
	let showPublicDecks = false;
	let searchDeck = "";
	let showMyDecks = true;
	let showSubscribedDecks = true;

	let adminShowBlockedDecks = true;
	let adminShowDeletedDecks = true;
	let adminSearch = "";
	let page = 'my-decks';

	let navButtons = [
		{ text: `Public Decks  <kbd class="ml-2 kbd">⌘+k</kbd>`, action: ()=>showPublicDecks=true},
		{ text: "Create Deck", href: "create-deck" },
	]

	if (navigator.userAgent.indexOf("Mac") != -1) {
		navButtons[0].text = `Public Decks  <kbd class="ml-2 kbd">⌘+k</kbd>`;
	}
	else{
		navButtons[0].text = `Public Decks  <kbd class="ml-2 kbd">Ctrl+K</kbd>`;
	}

	if ($userPermissionsStore.includes("ADMIN")) {
		navButtons.splice(2, 0, { text: "Admin", href: "admin" });
	}
	
	async function getAllDecks(){
		let res = await fetching("/api/get-all-decks", "GET");
		if(res.success){
			allDecks = res.items;
			return res.items;
		} 
		else{
			addToast("Error fetching all decks", "alert-error");
		}
	}
	

	async function getUserDecks(){
		let res = await fetching("/api/get-created-decks", "GET");
		if(res.success){
			userDecks = res.items;
			userDecks=[...userDecks];
			return res.items;
		} 
		else{
			addToast("Error fetching created decks", "alert-error");
		} 

	}
	
	
	async function getSubscribedDecks(){
		let res = await fetching("/api/get-subscribed-decks", "GET");
		if(res.success){
			subscribedDecks = res.items;
			return res.items;
		} 
		else{
			addToast("Error fetching subscribed decks", "alert-error");
		}
	}

	
	async function getDecks(){
		if($userPermissionsStore.includes("ADMIN")){
			allDecks = await getAllDecks();
		}
		getUserDecks();
		getSubscribedDecks();
	}

	$: {
		if(selectedDeck){
			let index = userDecks.findIndex(deck => deck.deckId == selectedDeck.deckId);
			if(index != -1){
				userDecks[index] = selectedDeck;
				userDecks = [...userDecks];
			}
		}
	}
</script>

<svelte:head>
	<link rel="icon" type="image/png" href="/favicon.png"/>
	<title>Decks overview</title>
    <script src="http://localhost:35729/livereload.js"></script>
</svelte:head>

<SvelteToast />
<Nav title="Decks" buttons={navButtons} />
<main class="m-20">
	{#if showEditDeckModal}
		<EditDeckModal bind:showEditDeckModal getDecks={getUserDecks} bind:selectedDeck />
	{/if}
	{#if showPublicDecks}
		<PublicDecksModal bind:showPublicDecks bind:selectedDeck bind:listCards on:refresh={getDecks} />
	{/if}
	{#if listCards}
		<ListCardsModal bind:listCards {selectedDeck} />
	{/if}

	{#if $userPermissionsStore.includes('ADMIN')}
		<div class="btn-group flex justify-center mb-8">
			<button class="btn {page=="my-decks" ? "btn-active" : ""}" on:click={()=>page="my-decks"}>My Decks</button>
			<button class="btn {page=="all-decks" ? "btn-active" : ""}" on:click={()=>page="all-decks"}>All Decks</button>
		</div>
	{/if}

	{#if page == "my-decks"}
		<div class="flex justify-center">
			<div class="flex justify-center mx-auto w-full">
				<label class="input-group flex justify-center w-full ">
					<span><i class="bi bi-search"/></span>
					<input type="text" placeholder="Search for Deck" class="input input-bordered bg-slate-900" bind:value={searchDeck}/>
				</label>
			</div>
		</div>
		<div>
			<div class="flex">
				<h1 class="text-4xl font-bold m-2">My Decks</h1>
				<div class="flex items-center">
					<input type="checkbox" class="flex items-center toggle toggle-info" bind:checked={showMyDecks} />
				</div>
			</div>
			{#if showMyDecks}
			<div>
				{#if userDecks.length == 0}
					<h1 class="text-2xl m-2">No Decks Found</h1>
				{:else}
					<div class="grid grid-cols-4 gap-4">
							{#each userDecks as deck (deck.deckId)}
								{#if deck.name.includes(searchDeck) && deck.name.includes(searchDeck)}
									<div in:fly={{y: -100, duration: 300}} out:fly={{y: 100, duration: 300}}>
										{#key deck}
										<Deck 
											{deck}
											on:editDeck={()=> {selectedDeck = deck; showEditDeckModal = true}}
											on:editCards={()=> {$userSelectedDeckStore = deck; redirect("edit-cards")}}
											on:learnDeck={()=> {$userSelectedDeckStore = deck; redirect("learn")}}
											on:listCards={()=> {listCards=true; selectedDeck = deck}}
											on:deleteDeck={async ()=> {await getUserDecks(); userDecks=[...userDecks];}}
										/>
										{/key}
									</div>
								{:else}
									<h1>No deck found</h1>
								{/if}
							{/each}
					</div>
				{/if}
			</div>
			{/if}
		</div>

		<div>
			<div class="flex">
				<h1 class="text-4xl font-bold m-2">Subscribed Decks</h1>
				<div class="flex items-center">
					<input type="checkbox" class="flex items-center toggle toggle-info" bind:checked={showSubscribedDecks} />
				</div>
			</div>
			{#if showSubscribedDecks}
			<div>
				{#if subscribedDecks.length == 0}
					<h1 class="text-2xl m-2">No subscribed Decks</h1>
				{:else}
					<div class="grid grid-cols-4 gap-4">
						{#each subscribedDecks as deck (deck.deckId)}
							{#if deck.name.includes(searchDeck) && deck.name.includes(searchDeck)}
								<div in:fly={{y: -100, duration: 300}} out:fly={{y: 100, duration: 300}}>
									<SubscribedDeck
										{deck}
										on:learnDeck={()=> {$userSelectedDeckStore = deck; redirect("learn")}}
										on:listCards={()=> {listCards=true; selectedDeck = deck}}
										on:unsubscribe={()=>getSubscribedDecks()}										
									/>
								</div>
								{:else}
								<h1>No deck found</h1>
							{/if}
						{/each}
					</div>	
				{/if}
			</div>
			{/if}
		</div>
	{/if}

	{#if page == "all-decks" && $userPermissionsStore.includes('ADMIN')}
			<div class="flex flex-row justify-center">
				<div class="form-control w-auto">
					<label class="cursor-pointer label">
					<h1 class="text-xl mx-2">Show blocked Decks:</h1> 
					<input type="checkbox" class="toggle toggle-info toggle-lg" bind:checked={adminShowBlockedDecks} />
					</label>
				</div>
				<div class="form-control w-auto">
					<label class="cursor-pointer label">
					<h1 class="text-xl mx-2">Show deleted Decks:</h1> 
					<input type="checkbox" class="toggle toggle-info toggle-lg" bind:checked={adminShowDeletedDecks}  />
					</label>
				</div>
				
			</div>
			
			<div class="flex justify-center mx-auto w-full">
				<label class="input-group flex justify-center w-full ">
				<span><i class="bi bi-search"/></span>
				<input type="text" placeholder="Search for Deck" class="input input-bordered bg-slate-900" bind:value={adminSearch}/>
				</label>
			</div>

		<br class="mt-4"/>


		<div>
			{#await getAllDecks()}
				<Spinner/>
			{:then allDecks}
				{#if allDecks.length == 0}
					<h1 class="text-2xl font-bold flex justify-center">No Decks</h1>
				{:else}
					{#key allDecks}
						<div class="grid grid-cols-4 gap-4">
							{#each allDecks as deck}
								{#if deck.name.includes(adminSearch) || deck.description.includes(adminSearch)}
									{#if adminShowBlockedDecks && deck.blocked}
										<AdminDeck {deck}/>
									{:else if adminShowDeletedDecks && deck.deleted}
										<AdminDeck {deck}/>
									{:else if !deck.blocked && !deck.deleted}
										<AdminDeck {deck}/>
									{/if}
								{:else}
									<h1>No deck found</h1>
								{/if}
							{/each}
						</div>
					{/key}
				{/if}
			{/await}
		</div>
	{/if}
</main>